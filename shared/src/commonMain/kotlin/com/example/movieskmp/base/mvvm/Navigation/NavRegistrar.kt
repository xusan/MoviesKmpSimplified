package com.base.mvvm.Navigation

import com.base.mvvm.ViewModels.PageViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.module.Module
import org.koin.dsl.module
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

//This class used to map ViewModel to Page.
//Currently, this class only used by Kotlin. Swift side has its own implementation.
@OptIn(ExperimentalObjCName::class)
@ObjCName(name = "DO_NOT_USE_ON_SWIFT_NavRegistrar", exact = true) //Do not use this class on swift
class NavRegistrar : KoinComponent
{
    public val navPages = mutableListOf<NavPageInfo>()
    public val modules: MutableList<Module> = mutableListOf()

    /**
     * Registers a ViewModel–Page pair for navigation.
     *
     * @param createPage factory lambda to create a page
     * @param createViewModel factory lambda to create its ViewModel
     */
    inline fun <reified TViewModel : PageViewModel, reified TPage : IPage> RegisterPageForNavigation(noinline createPage: () -> TPage, noinline createViewModel: () -> TViewModel)
    {
        // Dynamically register page and viewModel in Koin
        val pages = module()
        {
            factory<TPage> { createPage() }
            factory<TViewModel> { createViewModel() }
        }
        modules += pages;


        val vmName = TViewModel::class.simpleName ?: throw IllegalArgumentException("Cannot get ViewModel name for ${TViewModel::class}")

        // register in navPages
        if (navPages.none { it.vmName == vmName })
        {
            navPages.add(NavPageInfo(vmName = vmName,
                                     createPageFactory = { get<TPage>() },
                                     createVmFactory = { get<TViewModel>() }))
        }
    }

    inline fun <reified TViewModel : PageViewModel> CreatePage(parameters: INavigationParameters): IPage
    {
        val vmName = TViewModel::class.simpleName
            ?: throw IllegalArgumentException("ViewModel class has no simple name.")
        return CreatePage(vmName, parameters)
    }

    /**
     * Creates a page and binds its ViewModel using the stored registration.
     */
    fun CreatePage(vmName: String, parameters: INavigationParameters): IPage
    {
        val pageInfo = navPages.firstOrNull { it.vmName == vmName }

        if(pageInfo == null)
        {
            throw IllegalArgumentException("ViewModel '$vmName' was not registered for navigation.")
        }

        val page = pageInfo.createPageFactory()
        val vm = pageInfo.createVmFactory()

        page.ViewModel = vm
        vm.Initialize(parameters)

        return page
    }
}


//**********************************USAGE***************************************

//val registrar = NavRegistrar()
//val appModule = module {
//    single { registrar } // register as a instance
      //other app services
//    single<IMyService> { MyServiceImpl() }
//    single<IDbTransferService> { DbTransferService() }
//}
//registrar.registerPageForNavigation(pageFactory = { MyPage() },viewModelFactory = { MyPageViewModel() })
//startKoin {
//    modules(appModule, registrar.module)
//}

//use CreatePage() in services
//val registrar: NavRegistrar by inject()
///registrar.CreatePage<TViewModel>(parameters)
