package com.base.mvvm.Droid.Navigation

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.base.abstractions.Diagnostic.ILogging
import com.base.abstractions.Diagnostic.ILoggingService
import com.base.abstractions.Diagnostic.SpecificLoggingKeys
import com.base.impl.ContainerLocator
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
import com.example.movieskmp.base.mvvm.Navigation.INavUiSynchronizer
import com.example.movieskmp.base.mvvm.Navigation.IViewModelProvider
import kotlinx.coroutines.delay
import com.example.movieskmp.shared.R.*

//NOTE:
// We intentionally use commitAllowingStateLoss() when making transaction on FragmentManager.
// FragmentManager is treated as a UI cache, not the source of truth.
// The authoritative navigation state is maintained in our own
// ViewModel-based navigation stack (navStack).
// If this transaction is dropped due to state loss (e.g. config change),
// the UI will be reconciled from the navigation stack(navStack) on next restore
// via syncWithNavigationState()(The root Activity must call it when restore).
class DroidPageNavigationFrameLayout : FrameLayout, IPageNavigationService, IViewModelProvider, INavUiSynchronizer
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

    internal val navStack: MutableList<PageViewModel> = mutableListOf()
    internal var currentViewModel: PageViewModel? = null

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

    fun Initialize()
    {
        if(!isInitialized)
        {
            val loggingService = ContainerLocator.Resolve<ILoggingService>()
            specificLogger = loggingService.CreateSpecificLogger(SpecificLoggingKeys.LogUINavigationKey)
        }
    }

    fun AttachTo(container: ViewGroup)
    {
        val currentParent = parent as? ViewGroup
        if (currentParent === container) return

        currentParent?.removeView(this)

        container.addView(
            this,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
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
        val oldViewModel = currentViewModel
        val newPage = navRegistrar.CreatePage(vmName, parameters) as DroidLifecyclePage
        val newViewModel = newPage.ViewModel
        currentViewModel = newViewModel

        //save new page in local stack list
        newPage.pushNavAnimated = animated
        navStack.add(newViewModel)

        //push new page to ui stack
        val pushTransaction = FragmentManager.beginTransaction()
        if (animated)
        {
            pushTransaction.setCustomAnimations(anim.slide_right_in, anim.slide_right_out)
        }
        pushTransaction.add(id, newPage, newViewModel.InstanceId)
        // We intentionally use commitAllowingStateLoss() here.
        // FragmentManager is treated as a UI cache, not the source of truth.
        // The authoritative navigation state is maintained in our own
        // ViewModel-based navigation stack.
        // If this transaction is dropped due to state loss (e.g. config change),
        // the UI will be reconciled from the navigation stack on next restore
        // via syncWithNavigationState().
        pushTransaction.commitAllowingStateLoss()


        //call viewmodel lifecycle methods
        oldViewModel?.OnNavigatedFrom(NavigationParameters())
        newViewModel.OnNavigatedTo(parameters)

        //hide keyboard if open
        context.HideKeyboard(this)

        //TODO: This solution is ugly. Instead of this: we need to listen/wait
        // the onResume() callbacks of the fragment - this way we can make sure that fragment is finished to navigate
        //set some delay to make sure page completely finished to navigate(animation end)
        if (animated)
        {
            delay(animationDuration.toLong())
        }

        //hide current page
        oldViewModel?.let()
        {
            val oldPage = fragmentFor(it);
            val hideTransaction = FragmentManager.beginTransaction()
            hideTransaction.hide(oldPage)
            hideTransaction.commitAllowingStateLoss()
        }
    }


    private suspend fun OnPopAsync(parameters: INavigationParameters)
    {
        SpecificLogMethodStart(::NavigateToRoot.name)

        if (navStack.size == 1)
        {
            return
        }

        val popViewModel = currentViewModel!!
        val popPage = fragmentFor(popViewModel)
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
        navStack.remove(popViewModel)

        //show beneath page
        val toShowViewModel = navStack.last()
        val toShowPage = fragmentFor(toShowViewModel)
        val showTransaction = FragmentManager.beginTransaction()
        showTransaction.show(toShowPage)
        showTransaction.commitAllowingStateLoss()

        //call viewmodel lifecycle methods
        currentViewModel = toShowViewModel
        popViewModel.OnNavigatedFrom(NavigationParameters())
        popViewModel.Destroy()
        toShowViewModel.OnNavigatedTo(parameters)

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
        val removedViewModels = mutableListOf<PageViewModel>()
        val splitedCount = url.split('/').size - 1
        for (i in 0 until splitedCount)
        {
            val vmToRemove = navStack.lastOrNull()
            if (vmToRemove == null)
            {
                //this can happen if user somehow removed this page for example: tapped device back while app removes this page, or double tap
                Logger.LogWarning("${DroidPageNavigationFrameLayout::class.simpleName}: Canceling OnMultiPopAsync() because pageToRemove is null")
                return
            }
            navStack.remove(vmToRemove)
            removedViewModels.add(vmToRemove)
        }

        val hideTransaction = FragmentManager.beginTransaction()
        if (animated)
        {
            hideTransaction.setCustomAnimations(anim.slide_right_in, anim.slide_right_out)
        }
        val firstPoppingPage = fragmentFor(currentViewModel!!)
        hideTransaction.hide(firstPoppingPage)

        //first: show destination page
        val desViewModel = navStack.last()
        val desPage = fragmentFor(desViewModel)
        val showTransaction = FragmentManager.beginTransaction()
        showTransaction.show(desPage)
        showTransaction.commitAllowingStateLoss()
        currentViewModel = desViewModel

        //then: start pop animation
        hideTransaction.commitAllowingStateLoss()


        //call viewmodel lifecycle methods
        currentViewModel!!.OnNavigatedTo(parameters)

        //hide keyboard if open
        context.HideKeyboard(this)


        if (animated)
        {
            delay(animationDuration.toLong())
        }
        //removed pages after navigating to destination
        val removeTransaction = FragmentManager.beginTransaction()
        for (poppedVm in removedViewModels)
        {
            val poppedPage = fragmentFor(poppedVm)
            removeTransaction.remove(poppedPage)
            poppedVm.Destroy()
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

        val newPage = navRegistrar.CreatePage(vmName, parameters) as DroidLifecyclePage
        val newViewModel = newPage.ViewModel
        navStack.add(newViewModel)
        pushTransaction.add(id, newPage, newViewModel.InstanceId)
        currentViewModel = newViewModel

        pushTransaction.commitAllowingStateLoss()

        //call viewmodel lifecycle methods
        newViewModel.OnNavigatedTo(parameters)

        //hide keyboard if open
        context.HideKeyboard(this)

        //removed pages after navigating to destination
        val removeTransaction = FragmentManager.beginTransaction()
        val splitedCount = url.split('/').size - 1

        for (i in 1..splitedCount)
        {
            val viewModelToRemove = navStack.last { p -> p != currentViewModel }
            val pageToRemove = fragmentFor(viewModelToRemove)
            navStack.remove(viewModelToRemove)
            removeTransaction.remove(pageToRemove)
            viewModelToRemove.Destroy()
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
        val newPage = navRegistrar.CreatePage(vmName, parameters) as DroidLifecyclePage
        val newViewModel = newPage.ViewModel
        navStack.add(newViewModel)
        currentViewModel = newViewModel

        //remove other pages except currentPage, it will become root page
        val removedViewModels = navStack.filter { p -> p != currentViewModel }
        //clear local stack list
        navStack.removeAll(removedViewModels)

        //add page to ui stack
        val pushTransaction = FragmentManager.beginTransaction()
        if (animated)
        {
            pushTransaction.setCustomAnimations(anim.slide_right_in, anim.slide_right_out)
        }
        pushTransaction.add(id, newPage, newViewModel.InstanceId)
        pushTransaction.commitAllowingStateLoss()

        //call viewmodel lifecycle methods
        newViewModel.OnNavigatedTo(parameters)

        //hide keyboard if open
        context.HideKeyboard(this)

        //if navigation is animated then wait for compilation
        if (animated)
        {
            delay(animationDuration.toLong())
        }


        val removeTransaction = FragmentManager.beginTransaction()
        for (vmToRemove in removedViewModels)
        {
            val pageToRemove = fragmentFor(vmToRemove)
            removeTransaction.remove(pageToRemove)
            vmToRemove.Destroy()
        }
        removeTransaction.commitAllowingStateLoss()
    }

    private suspend fun OnMultiPushRootAsync(url: String, parameters: INavigationParameters, animated: Boolean)
    {
        SpecificLogMethodStart(::OnMultiPushRootAsync.name, url)
        //remove existing pages
        val removedViewModels = navStack.toList()
        //clear local stack list
        navStack.clear()

        //create page and save it to local stack list
        val vmPages = url.split("/").filter { s -> s.isNotEmpty() }
        val pushTransaction = FragmentManager.beginTransaction()
        if (animated)
        {
            pushTransaction.setCustomAnimations(anim.slide_right_in, anim.slide_right_out)
        }

        for (vmName in vmPages)
        {
            val pageToPush = navRegistrar.CreatePage(vmName, parameters) as DroidLifecyclePage
            val viewModelToPush = pageToPush.ViewModel
            //add page to ui stack
            pageToPush.pushNavAnimated = animated
            navStack.add(viewModelToPush)

            pushTransaction.add(id, pageToPush, viewModelToPush.InstanceId)
            if (vmName == vmPages.last())
            {
                currentViewModel = viewModelToPush
            }
            else
            {
                pushTransaction.hide(pageToPush)
            }
        }

        pushTransaction.commitAllowingStateLoss()

        //call viewmodel lifecycle methods
        currentViewModel!!.OnNavigatedTo(parameters)

        //hide keyboard if open
        context.HideKeyboard(this)

        //if navigation is animated, then wait for compilation
        if (animated)
        {
            delay(animationDuration.toLong())
        }

        if (removedViewModels.isNotEmpty())
        {
            //remove other pages except the currentPage, it will become the root page
            val removeTransaction = FragmentManager.beginTransaction()
            for (vm in removedViewModels)
            {
                val page = fragmentFor(vm)
                removeTransaction.remove(page)
                vm.Destroy()
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
            val rootViewModel = navStack.first()
            val rootPage = fragmentFor(rootViewModel)
            //show root page
            val showTransaction = FragmentManager.beginTransaction()
            showTransaction.show(rootPage)
            showTransaction.commitAllowingStateLoss()

            val removedViewModels = mutableListOf<PageViewModel>()
            val popAnimTransaction = FragmentManager.beginTransaction()
            while (navStack.size > 1)
            {
                val vmToHide = navStack.last()
                navStack.remove(vmToHide)
                removedViewModels.add(vmToHide)

                if (vmToHide == currentViewModel)
                {
                    //hide current page with animation
                    val currentPage = fragmentFor(vmToHide)
                    popAnimTransaction.setCustomAnimations(anim.slide_right_in, anim.slide_right_out)
                    popAnimTransaction.hide(currentPage)
                }
            }

            popAnimTransaction.commitAllowingStateLoss()

            currentViewModel = rootViewModel
            rootViewModel.OnNavigatedTo(parameters)

            //hide keyboard if open
            context.HideKeyboard(this)

            delay(animationDuration.toLong())

            val removeTransaction = FragmentManager.beginTransaction()
            for (vm in removedViewModels)
            {
                val page = fragmentFor(vm)
                removeTransaction.remove(page)
                vm.Destroy()
            }
            removeTransaction.commitAllowingStateLoss()
        }
    }

    override fun GetCurrentPageModel(): PageViewModel?
    {
        SpecificLogMethodStart(::GetCurrentPageModel.name)
        val vm = navStack.lastOrNull()
        return vm
    }

    override fun GetRootPageModel(): PageViewModel?
    {
        SpecificLogMethodStart(::GetRootPageModel.name)

        val vm = navStack.firstOrNull()
        return vm
    }

    override fun GetCurrentPage(): IPage?
    {
        SpecificLogMethodStart(::GetCurrentPage.name)
        val vm = navStack.lastOrNull()
        vm?.let {
            val page = fragmentFor(vm)
            return page
        }

        return null
    }

    override fun GetNavStackModels(): List<PageViewModel>
    {
        SpecificLogMethodStart(::GetNavStackModels.name)
        val viewModels = navStack.toList()
        return viewModels
    }

    private fun fragmentFor(vm: PageViewModel): DroidLifecyclePage
    {
        val fragment = FragmentManager.findFragmentByTag(vm.InstanceId)
        val page = fragment as DroidLifecyclePage;
        return page;
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

    override fun FetchViewModel(id: String): PageViewModel?
    {
        val viewModel = navStack.firstOrNull {s-> s.InstanceId == id}
        return viewModel
    }

    /**
     * Synchronizes FragmentManager UI with the current navigation state(with our own logical nav stack).
     *
     * Source of truth:
     *  - Navigation stack (navStack)
     *
     * FragmentManager is treated as a cache:
     *  - Fragments may be missing
     *  - Fragments may be stale
     *  - Fragment visibility may be incorrect (after state loss / config change)
     *
     * This method is idempotent and safe to call:
     *  - after configuration change
     *  - after process restore
     *  - after state loss
     */
    override fun SyncWithNavigationState()
    {
        SpecificLogMethodStart(::SyncWithNavigationState.name)
        if(navStack.size == 0)
        {
            specificLogger.Log("DroidPageNavigationFrameLayout: Skip SyncWithNavigationState() as navStack is empty")
            return
        }

        val fm = FragmentManager
        val tx = fm.beginTransaction()

        val validIds = navStack.map { it.InstanceId }.toSet()
        val fragments = fm.fragments

        // 1) Hide everything first (visibility safety)
        specificLogger.Log("DroidPageNavigationFrameLayout: SyncWithNavigationState(): Current fragments count: ${fragments.count()}")
        for (fragment in fragments)
        {
            if (fragment.isAdded)
            {
                specificLogger.Log("DroidPageNavigationFrameLayout: SyncWithNavigationState(): Hiding fragment: $fragment")
                tx.hide(fragment)
            }
            else
            {
                specificLogger.LogWarning("DroidPageNavigationFrameLayout: SyncWithNavigationState(): can not hide fragment because its isAdded:false, fragment: $fragment")
            }
        }

        // 2) Remove stale fragments
        for (fragment in fragments)
        {
            val tag = fragment.tag ?: continue
            if (tag !in validIds)
            {
                specificLogger.LogWarning("DroidPageNavigationFrameLayout: SyncWithNavigationState(): removing fragment because can not be found in navStack: $fragment")
                tx.remove(fragment)
            }
        }

        // 3) Ensure top fragment exists
        val topVm = navStack.lastOrNull()
        if (topVm != null)
        {
            val tag = topVm.InstanceId

            var topFragment = fm.findFragmentByTag(tag)
            if(topFragment == null)
            {
                specificLogger.LogWarning("DroidPageNavigationFrameLayout: SyncWithNavigationState(): topFragment is not found recreating it, topVm: ${topVm::class.simpleName}")
                val vmName = topVm::class.simpleName!!
                topFragment = navRegistrar.CreatePage(vmName, NavigationParameters()) as Fragment
                tx.add(id, topFragment, topVm.InstanceId)
            }

            specificLogger.Log("DroidPageNavigationFrameLayout: SyncWithNavigationState(): showing topFragment, topVm: ${topVm::class.simpleName}")
            // 4) Show only the top fragment
            tx.show(topFragment)
        }
        else
        {
            specificLogger.LogWarning("DroidPageNavigationFrameLayout: SyncWithNavigationState(): can not show topFragment because navStack empty")
        }

        // 5) Commit reconciliation
        tx.commitAllowingStateLoss()
    }
}