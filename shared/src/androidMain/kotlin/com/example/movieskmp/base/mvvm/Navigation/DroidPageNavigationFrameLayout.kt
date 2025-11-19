package com.base.mvvm.Droid.Navigation

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.base.abstractions.Diagnostic.ILogging
import com.base.abstractions.Diagnostic.ILoggingService
import com.base.abstractions.Diagnostic.SpecificLoggingKeys
import com.base.impl.ContainerLocator
import com.base.impl.Diagnostic.LoggableService
import com.base.impl.Droid.Utils.ContextExtensions.HideKeyboard
import com.base.impl.Droid.Utils.CurrentActivity
import com.base.mvvm.Droid.Navigation.Pages.DroidLifecyclePage
import com.base.mvvm.Navigation.INavigationParameters
import com.base.mvvm.Navigation.IPage
import com.base.mvvm.Navigation.IPageNavigationService
import com.base.mvvm.Navigation.NavRegistrar
import com.base.mvvm.Navigation.NavigationParameters
import com.base.mvvm.Navigation.UrlNavigationHelper
import com.base.mvvm.ViewModels.PageViewModel
import kotlinx.coroutines.delay
import com.example.movieskmp.shared.R.*

class DroidPageNavigationFrameLayout : FrameLayout, IPageNavigationService
{
    lateinit var specificLogger: ILogging

    constructor(context: Context) : super(context)
    {

    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    {
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    {
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)
    {
    }



    private var _disposed: Boolean = false
    private var animationDuration: Int = 250



    private val FragmentManager: FragmentManager
        get()
        {
            val activity = CurrentActivity.Instance as? FragmentActivity

            if (activity != null)
            {
                return activity.supportFragmentManager
            }

            throw Exception("Your MainActivity should be FragmentActivity in order to use this PageNavigationFrameLayout service. For example make MainActivity to derive from AppCompatActivity")
        }

    internal val navStack: MutableList<DroidLifecyclePage> = mutableListOf()
    internal var currentPage: DroidLifecyclePage? = null

    override val CanNavigateBack: Boolean
        get()
        {
            return navStack.size > 1
        }

    private var _logger: ILoggingService? = null
    val Logger: ILoggingService
        get()
        {
            if (_logger == null)
            {
                _logger = ContainerLocator.Resolve<ILoggingService>()
            }

            return _logger!!
        }

    private var _navRegistrar: NavRegistrar? = null
    val navRegistrar: NavRegistrar
        get()
        {
            if (_navRegistrar == null)
            {
                _navRegistrar = ContainerLocator.Resolve<NavRegistrar>()
            }

            return _navRegistrar!!
        }

    private var isInitialized = false

    override fun onAttachedToWindow()
    {
        super.onAttachedToWindow()

        if (!isInitialized)
        {
            isInitialized = true
            initialize()
        }
    }

    private fun initialize() {
        val loggingService = ContainerLocator.Resolve<ILoggingService>()
        specificLogger = loggingService.CreateSpecificLogger(SpecificLoggingKeys.LogUINavigationKey)
    }

    override suspend fun Navigate(url: String, parameters: INavigationParameters?, useModalNavigation: Boolean, animated: Boolean, wrapIntoNav: Boolean)
    {
        try
        {
            SpecificLogMethodStart(::Navigate.name, url)
            val params = parameters ?: NavigationParameters()

            val navInfo = UrlNavigationHelper.Companion.Parse(url)

            if (navInfo.isPush)
            {
                OnPushAsync(url, params, animated)
            }
            else if (navInfo.isPop)
            {
                OnPopAsync(params)
            }
            else if (navInfo.isMultiPop)
            {
                OnMultiPopAsync(url, params, animated)
            }
            else if (navInfo.isMultiPopAndPush)
            {
                OnMultiPopAndPush(url, params, animated)
            }
            else if (navInfo.isPushAsRoot)
            {
                OnPushRootAsync(url, params, animated)
            }
            else if (navInfo.isMultiPushAsRoot)
            {
                OnMultiPushRootAsync(url, params, animated)
            }
            else
            {
                throw NotImplementedError("Navigation case is not implemented.")
            }
        }
        catch (ex: Exception)
        {
            Logger.TrackError(ex)
            PrintCurrentStack()
        }
    }

    override suspend fun NavigateToRoot(parameters: INavigationParameters?)
    {
        try
        {
            SpecificLogMethodStart(::NavigateToRoot.name)
            val params = parameters ?: NavigationParameters()
            OnPopToRootAsync(params)
        }
        catch (ex: Exception)
        {
            Logger.TrackError(ex)
        }
    }

    private suspend fun OnPushAsync(vmName: String, parameters: INavigationParameters, animated: Boolean)
    {
        SpecificLogMethodStart(::NavigateToRoot.name, vmName)
        //create new page
        val oldPage = currentPage
        val newPage = navRegistrar.CreatePage(vmName, parameters) as DroidLifecyclePage
        currentPage = newPage

        //save new page in local stack list
        newPage.pushNavAnimated = animated
        navStack.add(newPage)

        //push new page to ui stack
        val pushTransaction = FragmentManager.beginTransaction()
        if (animated)
        {
            pushTransaction.setCustomAnimations(anim.slide_right_in, anim.slide_right_out)
        }
        pushTransaction.add(id, newPage)
        pushTransaction.commitAllowingStateLoss()

        //call viewmodel lifecycle methods
        oldPage?.ViewModel?.OnNavigatedFrom(NavigationParameters())
        newPage.ViewModel.OnNavigatedTo(parameters)

        //hide keyboard if open
        context.HideKeyboard(this)

        if (animated)
        {
            delay(animationDuration.toLong())
        }

        //hide current page
        val hideTransaction = FragmentManager.beginTransaction()
        oldPage?.let { hideTransaction.hide(it) }
        hideTransaction.commitAllowingStateLoss()
    }

    private suspend fun OnPopAsync(parameters: INavigationParameters)
    {
        SpecificLogMethodStart(::NavigateToRoot.name)

        if (navStack.size == 1)
        {
            return
        }

        val popPage = currentPage!!
        val animated = popPage.pushNavAnimated
        //hide poped page
        val hideTransaction = FragmentManager.beginTransaction()

        if (animated)
        {
            hideTransaction.setCustomAnimations(anim.slide_right_in, anim.slide_right_out)
        }

        hideTransaction.hide(popPage)
        hideTransaction.commitAllowingStateLoss()

        //remove from local stack list
        navStack.remove(popPage)

        //show beneath page
        val toShowPage = navStack.last()
        val showTransaction = FragmentManager.beginTransaction()
        showTransaction.show(toShowPage)
        showTransaction.commitAllowingStateLoss()

        //call viewmodel lifecycle methods
        currentPage = toShowPage
        popPage.ViewModel.OnNavigatedFrom(NavigationParameters())
        currentPage!!.ViewModel.OnNavigatedTo(parameters)

        //hide keyboard if open
        context.HideKeyboard(this)

        //if navigation is animated then wait for compilation
        if (animated)
        {
            delay(animationDuration.toLong())
        }

        //remove poped page
        val removeTransaction = FragmentManager.beginTransaction()
        removeTransaction.remove(popPage)
        removeTransaction.commitAllowingStateLoss()
    }

    private suspend fun OnMultiPopAsync(url: String, parameters: INavigationParameters, animated: Boolean)
    {
        SpecificLogMethodStart(::OnMultiPopAsync.name, url)
        val pagesToRemove = mutableListOf<DroidLifecyclePage>()
        val splitedCount = url.split('/').size - 1
        for (i in 0 until splitedCount)
        {
            val pageToRemove = navStack.lastOrNull()
            if (pagesToRemove == null)
            {
                //this can happen if user somehow removed this page for example: tapped device back while app removes this page, or double tap
                Logger.LogWarning("${DroidPageNavigationFrameLayout::class.simpleName}: Canceling OnMultiPopAsync() because pageToRemove is null")
                return
            }
            navStack.remove(pageToRemove)
            pagesToRemove.add(pageToRemove!!)
        }

        val hideTransaction = FragmentManager.beginTransaction()
        if (animated)
        {
            hideTransaction.setCustomAnimations(anim.slide_right_in, anim.slide_right_out)
        }
        hideTransaction.hide(currentPage!!)

        //first show home page
        currentPage = navStack.last()
        val showTransaction = FragmentManager.beginTransaction()
        showTransaction.show(currentPage!!)
        showTransaction.commitAllowingStateLoss()

        //then start pop animation
        hideTransaction.commitAllowingStateLoss()


        //call viewmodel lifecycle methods
        currentPage!!.ViewModel.OnNavigatedTo(parameters)

        //hide keyboard if open
        context.HideKeyboard(this)


        if (animated)
        {
            delay(animationDuration.toLong())
        }
        //removed pages after navigating to destination
        val removeTransaction = FragmentManager.beginTransaction()
        for (page in pagesToRemove)
        {
            removeTransaction.remove(page)
        }
        removeTransaction.commitAllowingStateLoss()
    }

    private suspend fun OnMultiPopAndPush(url: String, parameters: INavigationParameters, animated: Boolean)
    {
        SpecificLogMethodStart(::OnMultiPopAndPush.name, url)
        //push new page to ui stack
        val pushTransaction = FragmentManager.beginTransaction()
        if (animated)
        {
            pushTransaction.setCustomAnimations(anim.slide_right_in, anim.slide_right_out)
        }

        val vmName = url.replace("../", "")

        currentPage = navRegistrar.CreatePage(vmName, parameters) as DroidLifecyclePage
        navStack.add(currentPage!!)
        pushTransaction.add(id, currentPage!!)

        pushTransaction.commitAllowingStateLoss()

        //call viewmodel lifecycle methods
        currentPage!!.ViewModel.OnNavigatedTo(parameters)

        //hide keyboard if open
        context.HideKeyboard(this)

        //removed pages after navigating to destination
        val removeTransaction = FragmentManager.beginTransaction()
        val splitedCount = url.split('/').size - 1

        for (i in 1..splitedCount)
        {
            val pageToRemove = navStack.last { p -> p != currentPage }
            navStack.remove(pageToRemove)
            removeTransaction.remove(pageToRemove)
        }

        if (animated)
        {
            delay(animationDuration.toLong())
        }
        removeTransaction.commitAllowingStateLoss()
    }

    private suspend fun OnPushRootAsync(url: String, parameters: INavigationParameters, animated: Boolean)
    {
        SpecificLogMethodStart(::OnPushRootAsync.name, url)
        //create page and save it to local stack list
        val vmName = url.replace("/", "").replace("NavigationPage", "")
        currentPage = navRegistrar.CreatePage(vmName, parameters) as DroidLifecyclePage
        navStack.add(currentPage!!)

        //remove other pages except currentPage, it will become root page
        val pagesToRemove = navStack.filter { p -> p != currentPage }
        //clear local stack list
        navStack.removeAll(pagesToRemove)

        //add page to ui stack
        val pushTransaction = FragmentManager.beginTransaction()
        if (animated)
        {
            pushTransaction.setCustomAnimations(anim.slide_right_in, anim.slide_right_out)
        }
        pushTransaction.add(id, currentPage!!)
        pushTransaction.commitAllowingStateLoss()

        //call viewmodel lifecycle methods
        currentPage!!.ViewModel.OnNavigatedTo(parameters)

        //hide keyboard if open
        context.HideKeyboard(this)

        //if navigation is animated then wait for compilation
        if (animated)
        {
            delay(animationDuration.toLong())
        }


        val removeTransaction = FragmentManager.beginTransaction()
        for (page in pagesToRemove)
        {
            removeTransaction.remove(page)
        }
        removeTransaction.commitAllowingStateLoss()
    }

    private suspend fun OnMultiPushRootAsync(url: String, parameters: INavigationParameters, animated: Boolean)
    {
        SpecificLogMethodStart(::OnMultiPushRootAsync.name, url)
        //remove existing pages
        val pagesToRemove = navStack.toList()
        //clear local stack list
        navStack.clear()

        //create page and save it to local stack list
        val cleanUrl = url.replace("/NavigationPage", "")

        val vmPages = cleanUrl.split("/").filter { s -> s.isNotEmpty() }
        val pushTransaction = FragmentManager.beginTransaction()
        if (animated)
        {
            pushTransaction.setCustomAnimations(anim.slide_right_in, anim.slide_right_out)
        }

        for (vmName in vmPages)
        {
            val page = navRegistrar.CreatePage(vmName, parameters) as DroidLifecyclePage
            //add page to ui stack
            page.pushNavAnimated = animated
            navStack.add(page)

            if (vmName == vmPages.last())
            {
                currentPage = page
                pushTransaction.add(id, currentPage!!)
            }
            else
            {
                pushTransaction.add(id, page)
                pushTransaction.hide(page)
            }
        }

        pushTransaction.commitAllowingStateLoss()

        //call viewmodel lifecycle methods
        currentPage!!.ViewModel.OnNavigatedTo(parameters)

        //hide keyboard if open
        context.HideKeyboard(this)

        //if navigation is animated, then wait for compilation
        if (animated)
        {
            delay(animationDuration.toLong())
        }

        if (pagesToRemove.isNotEmpty())
        {
            //remove other pages except the currentPage, it will become the root page
            val removeTransaction = FragmentManager.beginTransaction()
            for (page in pagesToRemove)
            {
                removeTransaction.remove(page)
            }
            removeTransaction.commitAllowingStateLoss()
        }
    }

    private suspend fun OnPopToRootAsync(parameters: INavigationParameters)
    {
        SpecificLogMethodStart(::OnPopToRootAsync.name)

        if (navStack.size <= 1)
        {
            return
        }
        else if (navStack.size == 2)
        {
            OnPopAsync(parameters)
        }
        else
        {
            val rootPage = navStack.first()
            //show root page
            val showTransaction = FragmentManager.beginTransaction()
            showTransaction.show(rootPage)
            showTransaction.commitAllowingStateLoss()

            val pagesToRemove = mutableListOf<DroidLifecyclePage>()
            val popAnimTransaction = FragmentManager.beginTransaction()
            while (navStack.size > 1)
            {
                val pageToHide = navStack.last()
                navStack.remove(pageToHide)
                pagesToRemove.add(pageToHide)

                if (pageToHide == currentPage)
                {

                    //hide current page with animation
                    popAnimTransaction.setCustomAnimations(anim.slide_right_in, anim.slide_right_out)
                    popAnimTransaction.hide(pageToHide)
                }
            }

            popAnimTransaction.commitAllowingStateLoss()

            currentPage = rootPage
            currentPage!!.ViewModel.OnNavigatedTo(parameters)

            //hide keyboard if open
            context.HideKeyboard(this)

            delay(animationDuration.toLong())

            val removeTransaction = FragmentManager.beginTransaction()
            for (page in pagesToRemove)
            {
                removeTransaction.remove(page)
            }
            removeTransaction.commitAllowingStateLoss()
        }
    }

    override fun GetCurrentPageModel(): PageViewModel?
    {
        SpecificLogMethodStart(::GetCurrentPageModel.name)
        val page = navStack.lastOrNull()
        return page?.ViewModel
    }

    override fun GetRootPageModel(): PageViewModel?
    {
        SpecificLogMethodStart(::GetRootPageModel.name)

        val page = navStack.firstOrNull()
        return page?.ViewModel
    }

    override fun GetCurrentPage(): IPage?
    {
        SpecificLogMethodStart(::GetCurrentPage.name)
        val page = navStack.lastOrNull()
        return page
    }

    override fun GetNavStackModels(): List<PageViewModel>
    {
        SpecificLogMethodStart(::GetNavStackModels.name)
        val viewModels = navStack.map { x -> x.ViewModel }
        return viewModels
    }

    private fun PrintCurrentStack()
    {
        SpecificLogMethodStart(::PrintCurrentStack.name)
        val currentStack = GetNavStackModels()
        val currentUri = currentStack.joinToString("/")

        Logger.Log("${DroidPageNavigationFrameLayout::class.simpleName}: current stack: $currentUri")
    }

    fun SpecificLogMethodStart(methodName: String, vararg args: Any? )
    {
        try
        {
            val className = this::class.simpleName!!
            specificLogger.LogMethodStarted(className, methodName, args.toList())
        }
        catch (ex: Throwable)
        {
            println(ex.stackTraceToString())
        }
    }
}