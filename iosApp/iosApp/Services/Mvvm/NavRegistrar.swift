import SharedAppCore

import Foundation


class NavRegistrar {
    
    private(set) static var navPages: [NavPageInfo] = []
    
    // Registers a ViewModel–Page pair for navigation
    static func RegisterPageForNavigation<TViewModel: PageViewModel, TPage: IPage>(_
        createPage: @escaping () -> TPage,
        _ createViewModel: @escaping () -> TViewModel
    )
    {
        // Use the type name as identifier
        let vmName = String(describing: TViewModel.self)
        
        // Register into navPages only once per VM type
        if !navPages.contains(where: { $0.vmName == vmName })
        {
            let pageInfo = NavPageInfo(
                vmName: vmName,
                createPageFactory: { createPage() },
                createVmFactory: { createViewModel() }
            )
            
            navPages.append(pageInfo)
        }
    }
    
    // Generic overload for creating by type
    static func CreatePage<TViewModel: PageViewModel>(_ type: TViewModel.Type, parameters: INavigationParameters) -> IPage
    {
        let vmName = String(describing: TViewModel.self)
        return CreatePage(vmName: vmName, parameters: parameters)
    }
    
    // Creates a page and binds its ViewModel using stored registration.
    static func CreatePage(vmName: String, parameters: INavigationParameters) -> IPage
    {        
        guard let pageInfo = navPages.first(where: { $0.vmName == vmName }) else {
            fatalError("ViewModel '\(vmName)' was not registered for navigation.")
        }
        
        let page = pageInfo.createPageFactory()
        let vm = pageInfo.createVmFactory()
        
        page.ViewModel = vm        
        
        vm.Initialize(parameters: parameters)
        
        return page
    }
}


