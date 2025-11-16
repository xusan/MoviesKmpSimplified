package com.example.movieskmp

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.base.abstractions.Diagnostic.ILoggingService
import com.base.impl.ContainerLocator
import com.base.impl.Droid.Utils.CurrentActivity
import com.base.mvvm.Droid.Navigation.Pages.DroidLifecyclePage
import com.base.mvvm.Navigation.IPageNavigationService
import com.base.mvvm.ViewModels.PageViewModel
import kotlinx.coroutines.launch
import com.example.movieskmp.Controls.MainSideSheetDialog
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

        val bootstrap = Bootstrap(this)

        binding.apply {
            pageNavigationService = navContainer
            bootstrap.RegisterTypes(pageNavigationService)
        }

        loggingService = ContainerLocator.Resolve<ILoggingService>()
        this.loggingService.Log("####################################################- APPLICATION STARTED -####################################################");
        this.loggingService.Log("MainActivity.OnCreate()");

        lifecycleScope.launch() {
            bootstrap.NavigateToPageAsync(pageNavigationService);
        }

        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        )
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

