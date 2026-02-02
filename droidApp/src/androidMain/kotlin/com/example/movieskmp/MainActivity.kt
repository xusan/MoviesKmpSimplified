package com.example.movieskmp

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.base.abstractions.Diagnostic.IConditionalLogging
import com.base.abstractions.Diagnostic.ILoggingService
import com.base.abstractions.Diagnostic.SpecificLoggingKeys
import com.base.abstractions.Essentials.IMediaPickerService
import com.base.impl.ContainerLocator
import com.base.impl.Droid.Essentials.IActivityMediaPicker
import com.base.mvvm.Droid.Navigation.DroidPageNavigationFrameLayout
import com.base.mvvm.Droid.Navigation.Pages.DroidLifecyclePage
import com.base.mvvm.Navigation.IPageNavigationService
import com.base.mvvm.ViewModels.PageViewModel
import kotlinx.coroutines.launch
import com.example.movieskmp.Controls.MainSideSheetDialog
import com.example.movieskmp.base.mvvm.Navigation.INavUiSynchronizer
import com.example.movieskmp.databinding.ActivityMainBinding
import java.util.Locale


class MainActivity : AppCompatActivity()
{
    private lateinit var binding: ActivityMainBinding
    private lateinit var pageNavigationService: IPageNavigationService
    private lateinit var loggingService: ILoggingService
    private var sideSheetDialog: MainSideSheetDialog? = null

    override fun onCreate(savedInstanceState: Bundle?)
    {
        //enableEdgeToEdge()
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        SetupCustomServices()

        //navigate to first page (if it is not restoring state)
        if(savedInstanceState == null)
        {
            lifecycleScope.launch()
            {
                //navigate to root page
                val bootstrap = ContainerLocator.Resolve<Bootstrap>()
                bootstrap.NavigateToRootAsync();
            }
        }

        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        )
    }

    private fun SetupCustomServices()
    {
        loggingService = ContainerLocator.Resolve<ILoggingService>()
        //add nav frame layout to root view
        pageNavigationService = ContainerLocator.Resolve<IPageNavigationService>()

        val navSyncService = ContainerLocator.Resolve<INavUiSynchronizer>()
        val navFrameLayout = pageNavigationService as DroidPageNavigationFrameLayout
        navFrameLayout.id = R.id.navContainer
        navFrameLayout.Initialize()
        navFrameLayout.AttachTo(binding.layoutRoot)
        //sync the nav stack
        navSyncService.SyncWithNavigationState()

        //Media picker should be inited before onResume()
        val mediaPickerService = ContainerLocator.Resolve<IMediaPickerService>()
        (mediaPickerService as IActivityMediaPicker).Initilize(this)
        Handler(Looper.getMainLooper()).post {
            (mediaPickerService as IConditionalLogging).InitSpecificlogger(SpecificLoggingKeys.LogEssentialServices)
        }
    }

    // when user click on page we should hide keyboard
    override fun dispatchTouchEvent(ev: MotionEvent) : Boolean
    {
        try
        {
            val dispatchEventListener = pageNavigationService.GetCurrentPage() as DroidLifecyclePage;
            dispatchEventListener.DispatchTouchEvent(ev);

            return super.dispatchTouchEvent(ev);
        }
        catch (ex: Exception)
        {
            loggingService.LogWarning(ex.toString())
            return true;
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed()
    {
        //This method is called when Device's system back button is pressed (which is in bottom bar in Android)
        //We need to check if it is not a root page because we don't want to pop last page
        if (pageNavigationService.CanNavigateBack)
        {
            val currentPage = pageNavigationService.GetCurrentPage() as DroidLifecyclePage
            //We need to do Pop navigation only when Push navigation animation is completed.
            //This prevents bugs such as https://github.com/imtllc/utilla-app-QA/issues/2531#event-17787173104
            //It happens when user navigate to some page and tap on back system button quickly while push animation still in progress
            //The fix is to ignore back button while page push animation in progress
            if (currentPage.IsPageEnterAnimationCompleted)
            {
                //push animation is not in progress so we can do Pop navigation
                val currentPageVm = currentPage.ViewModel;
                currentPageVm.DoDeviceBackCommand();
            }
        }
        else
        {
            var currentVm = this.GetCurrentViewModel();
            loggingService?.LogWarning("MainActivity.OnBackPressed() is canceled because CanNavigateBack is false for current page. Seems current page is root page thus can not navigate back, page: $currentVm");
        }
    }
    fun SetCulture() {
        val locale = Locale("en", "US")
        Locale.setDefault(locale)

        // Optionally apply to current configuration (Android specific)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    fun GetCurrentViewModel(): PageViewModel? {
        return pageNavigationService.GetCurrentPageModel()
    }

    fun GetRootPageViewModel(): PageViewModel? {
        return pageNavigationService.GetRootPageModel()
    }

    fun GetCurrentPage(): DroidLifecyclePage? {
        return pageNavigationService.GetCurrentPage() as? DroidLifecyclePage
    }

    fun ShowSideSheet() {
        if (sideSheetDialog == null) {
            sideSheetDialog = MainSideSheetDialog(this).apply {
                setContentView(R.layout.page_main_sidesheet_view)
                setSheetEdge(Gravity.START)
            }
        }
        sideSheetDialog?.show()
    }
}

