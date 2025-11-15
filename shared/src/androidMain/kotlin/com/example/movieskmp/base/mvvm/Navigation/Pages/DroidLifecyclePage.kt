package com.base.mvvm.Droid.Navigation.Pages


import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.base.impl.ContainerLocator
import com.base.impl.Droid.Utils.CurrentActivity
import com.base.impl.Droid.Utils.ToVisibility
import com.base.mvvm.Navigation.IPageNavigationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max

open class DroidLifecyclePage : DroidBasePage(), OnApplyWindowInsetsListener {
    protected var txtTitle: TextView? = null
    protected var btnBack: Button? = null
    private var isVisible = false
    private var onApprearedSent = false
    private var loadingView: FrameLayout? = null
    private var txtLoadingMsg: TextView? = null
    private var pageAnimationListener: DroidPageEnterAnimationListner? = null
    private var rootLayout: ViewGroup? = null

    /**
     * Indicates is wether page was navigated with animation.
     * It is usefull when navigating back (pop) to check if we need to apply animation for navigation back,
     * so it will be consistent with forward (push) navigation.
     */
    var pushNavAnimated: Boolean = false

    var IsPageEnterAnimationCompleted: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loggingService.Log("${javaClass.simpleName}.OnCreate() (from base)")

        ViewModel.PropertyChanged += this::ViewModel_PropertyChanged
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        loggingService.Log("${javaClass.simpleName}.OnViewCreated() (from base)")

        super.onViewCreated(view, savedInstanceState)

        //this.txtTitle = view.FindViewById<TextView>(Resource.Id.txtTitle);

        val id = resources.getIdentifier("btnBack", "id", context?.packageName)
        this.btnBack = view.findViewById<Button>(id)
        if (this.btnBack != null) {
            this.btnBack!!.visibility = ViewModel.CanGoBack.ToVisibility()
            if (this.btnBack!!.visibility == View.VISIBLE) {
                this.btnBack!!.setOnClickListener { _ -> BtnBack_Click() }
            }
        }

        //add loading indicator
        //this.loadingView = (FrameLayout)LayoutInflater.From(this.Context).Inflate(Resource.Layout.fragment_loading_indicator, view as ViewGroup, false);
        //this.loadingView.Visibility = ViewStates.Gone;
        //this.txtLoadingMsg = this.loadingView.FindViewById<TextView>(Resource.Id.txtLoadingMsg);

        //set visibility for busy indicator via propertyChanged handler
        //this will check the model and set correct visibility for busy indicator
        ViewModel_PropertyChanged(ViewModel::BusyLoading.name)
        //pageLogger = logger.CreateSpecificLogger(AdvancedLogConstants.LogPageInsets);

        rootLayout = view as ViewGroup
        if (rootLayout is ScrollView) {
            val scrollView = rootLayout as ScrollView
            if (scrollView.childCount > 0)
                rootLayout = scrollView.getChildAt(0) as ViewGroup
        }

        //if (rootLayout != null)
        //{
        //    rootLayout.AddView(loadingView);
        //}

        HandleSoftInput()
    }

    /**
     * Used for Android 15>, older Android versions take a look IKeyboardResizeTypeService
     */
    private fun HandleSoftInput() {
        //starting from Android 15 we need to add
        //padding top/bottom so to not overlay the status/navigation bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            // Apply padding dynamically to your root layout
            ViewCompat.setOnApplyWindowInsetsListener(rootLayout!!, this)
        }
    }

    override fun onApplyWindowInsets(v: View, insets: WindowInsetsCompat): WindowInsetsCompat {
        return ApplyStandartPadding(v, insets)
    }

    /**
     * Sets top\bottom padding to page: to not overlay the status bar, bottom navigation bar
     */
    protected fun ApplyStandartPadding(v: View, insets: WindowInsetsCompat): WindowInsetsCompat {
        //get navigation bar insets
        val navBarInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars())

        //get status bar insets
        val statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars())

        //pageLogger.Log($"LifecyclePage.ApplyStandartPadding: statusBarInsets:{{{statusBarInsets.Left},{statusBarInsets.Top},{statusBarInsets.Right},{statusBarInsets.Bottom}}}");
        //pageLogger.Log($"LifecyclePage.ApplyStandartPadding: navBarInsets:{{{navBarInsets.Left},{navBarInsets.Top},{navBarInsets.Right},{navBarInsets.Bottom}}}");
        //pageLogger.Log($"LifecyclePage.ApplyStandartPadding: appTopPadding:{appTopPadding}");
        //pageLogger.Log($"LifecyclePage.ApplyStandartPadding: Density:{Resources.DisplayMetrics.Density}");
        //pageLogger.Log($"LifecyclePage.ApplyStandartPadding: statusBarHeightDp:{statusBarInsets.Top / Resources.DisplayMetrics.Density}");
        if (statusBarInsets.top > 0) { //no need to set on old android  < Android 15 (on old Android bellow 15 it will be 0)
            v.setPadding(v.paddingLeft, statusBarInsets.top, v.paddingRight, navBarInsets.bottom)
            //pageLogger.Log($"LifecyclePage.ApplyStandartPadding: Root layout padding:{{{v.PaddingLeft},{69},{v.PaddingRight},{navBarInsets.Bottom}}}");
        } else {
            //pageLogger.Log($"LifecyclePage.ApplyStandartPadding: Skip setting padding because top padding zero}}");
        }

        return insets
    }

    /**
     * Makes the same thing like ApplyStandartPadding() method but also adds additional
     * bottom padding when Keyboard appears to place any inputs/views above keyboard
     * This only works for Android 15 >
     */
    protected fun ApplyPaddingWithKeyboard(v: View, insets: WindowInsetsCompat): WindowInsetsCompat {
        // Insets for navigation/status bars
        val navBarInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
        val statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars())

        // Insets for the on-screen keyboard (IME)
        val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
        // the layout also has own padding top which used for Older(bellow Android 15).We need to remove it from final top padding
        //pageLogger.Log($"LifecyclePage.ApplyPaddingWithKeyboard: statusBarInsets:{{{statusBarInsets.Left},{statusBarInsets.Top},{statusBarInsets.Right},{statusBarInsets.Bottom}}}");
        //pageLogger.Log($"LifecyclePage.ApplyPaddingWithKeyboard: navBarInsets:{{{navBarInsets.Left},{navBarInsets.Top},{navBarInsets.Right},{navBarInsets.Bottom}}}");
        //pageLogger.Log($"LifecyclePage.ApplyPaddingWithKeyboard: imeInsets:{{{imeInsets.Left},{imeInsets.Top},{imeInsets.Right},{imeInsets.Bottom}}}");


        if (statusBarInsets.top > 0) { //no need to set on old android  < Android 15 (it will be zero on old Androids)
            val selectedBottomPadding = max(navBarInsets.bottom, imeInsets.bottom)
            val customTopPadding = statusBarInsets.top
            // Apply top, bottom, left, right padding
            v.setPadding(
                v.paddingLeft,
                customTopPadding,
                v.paddingRight,
                selectedBottomPadding
            ) // choose max to handle keyboard
            //pageLogger.Log($"LifecyclePage.ApplyPaddingWithKeyboard: Root layout padding:{{{v.PaddingLeft},{statusBarInsets.Top},{v.PaddingRight},{selectedBottomPadding}}}");
        }

        return insets
    }

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        loggingService.Log("${javaClass.simpleName}.OnCreateAnimation() (from base)")

        if (enter && IsPageEnterAnimationCompleted == false && nextAnim > 0) {
            pageAnimationListener = DroidPageEnterAnimationListner(this)
            val animation = AnimationUtils.loadAnimation(CurrentActivity.Instance, nextAnim)
            animation.setAnimationListener(pageAnimationListener)

            return animation
        } else {
            return super.onCreateAnimation(transit, enter, nextAnim)
        }
    }

    fun OnPageEnterAnimationCompleted() {
        IsPageEnterAnimationCompleted = true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        loggingService.Log("${javaClass.simpleName}.OnSaveInstanceState() (from base)")

        super.onSaveInstanceState(outState)
    }

    //#region Lifecycle Events
    override fun onHiddenChanged(hidden: Boolean) {
        loggingService.Log("${javaClass.simpleName}.OnHiddenChanged(hidden=$hidden) (from base)")

        super.onHiddenChanged(hidden)

        if (hidden) {
            ViewModel?.OnDisappearing()
            OnViewDisappearing()
        } else {
            ViewModel?.OnAppearing()
            OnViewAppearing()
        }
    }

    override fun onStart() {
        loggingService.Log("${javaClass.simpleName}.OnStart() (from base)")

        isVisible = true

        if (IsCurrentPage() && isVisible) {
            ViewModel?.OnAppearing()
            OnViewAppearing()
        }

        super.onStart()
    }

    override fun onResume() {
        loggingService.Log("${javaClass.simpleName}.OnResume() (from base)")

        super.onResume()

        isVisible = true

        if (onApprearedSent) //we want to send OnAppeared only once
            return

        if (IsCurrentPage() && isVisible) {
            onApprearedSent = true
            CoroutineScope(Dispatchers.Main).launch {
                delay(600)
                ViewModel?.OnAppeared()
                OnViewAppeared()
            }
        }
    }

    override fun onPause() {
        loggingService.Log("${javaClass.simpleName}.OnPause() (from base)")

        isVisible = false

        super.onPause()
    }

    override fun onStop() {
        loggingService.Log("${javaClass.simpleName}.OnStop() (from base)")

        isVisible = false
        if (IsCurrentPage()) {
            ViewModel?.OnDisappearing()
            OnViewDisappearing()
        }

        super.onStop()
    }

    protected open fun OnViewAppeared() {

    }

    protected open fun OnViewAppearing() {

    }

    protected open fun OnViewDisappearing() {

    }

    override fun onDestroyView() {
        loggingService.Log("${javaClass.simpleName}.OnDestroyView() (from base)")

        super.onDestroyView()

        //unsubscribe from SetOnApplyWindowInsetsListener
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            // Apply padding dynamically to your root layout
            ViewCompat.setOnApplyWindowInsetsListener(rootLayout as View, null)
        }
    }

    override fun onDestroy() {
        loggingService.Log("${javaClass.simpleName}.OnDestroy() (from base)")

        super.onDestroy()

        if (pageAnimationListener != null) {
            //pageAnimationListener!!.dispose()
            pageAnimationListener = null
        }

        ViewModel.PropertyChanged -= this::ViewModel_PropertyChanged
        ViewModel?.Destroy()
    }
    //#endregion

    private fun ViewModel_PropertyChanged(propertyName: String) {
        loggingService.Log("${javaClass.simpleName}.ViewModel_PropertyChanged(${propertyName})")

        //if (e.PropertyName == BusyLoadingProp)
        //{
        //    if (this.ViewModel.BusyLoading)
        //    {
        //        this.ShowLoadingIndicator(this.ViewModel.LoadingText);
        //    }
        //    else
        //    {
        //        this.HideLoadingIndicator();
        //    }
        //}
        //else if (e.PropertyName == ToastSeverityProp)
        //{
        //    severity = this.ViewModel.ToastSeverity;
        //}
        //else if (e.PropertyName == ToastMessageProp)
        //{
        //    if (!string.IsNullOrEmpty(this.ViewModel.ToastMessage))
        //    {
        //        MainActivity.Instance.ShowSnackBar(this.ViewModel.ToastMessage, severity);
        //    }
        //}
        //else
        //{
        OnViewModelPropertyChanged(propertyName)
        //}
    }

    protected open fun OnViewModelPropertyChanged(propertyName: String) {

    }

    private fun BtnBack_Click() {
        CoroutineScope(Dispatchers.Main).launch {
            ViewModel.BackCommand.ExecuteAsync()
        }
    }

    //public void ShowLoadingIndicator(string msg)
    //{
    //    this.txtLoadingMsg.Text = msg;
    //    this.loadingView.Visibility = ViewStates.Visible;
    //    this.loadingView.FadeTo(300, 0, 1, null);
    //}

    //public void HideLoadingIndicator()
    //{
    //    this.loadingView.FadeTo(300, 1, 0, () =>
    //    {
    //        this.loadingView.Visibility = ViewStates.Invisible;
    //    });
    //}

    private fun IsCurrentPage(): Boolean {
        val pageNavigationService = ContainerLocator.Resolve<IPageNavigationService>()
        val currentVisiblePage = pageNavigationService.GetCurrentPage()

        if (this != currentVisiblePage) {
            return false
        }

        return true
    }
}


