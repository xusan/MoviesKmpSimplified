import SharedAppCore
import AsyncDisplayKit
import UIKit

public class iOSPageNavigationController : ASDKNavigationController, IPageNavigationService
{
    internal var currentPage: iOSLifecyclePage?

    public init()
    {
        super.init(nibName: nil, bundle: nil)
        // hide top navigation bar
        self.setNavigationBarHidden(true, animated: false)
    }
    
    // Custom initializer that takes a root controller
    init(rootController: ASDKViewController<ASDisplayNode>)
    {
       super.init(rootViewController: rootController)
       self.setNavigationBarHidden(true, animated: false)
    }

    required init?(coder aDecoder: NSCoder)
    {
        super.init(coder: aDecoder)
        // hide top navigation bar
        self.setNavigationBarHidden(true, animated: false)
    }

    public var CanNavigateBack: Bool
    {
        return self.viewControllers.count > 1
    }
    
    @MainActor //attribute ensures this method is called in MAIN thread
    public func NavigateToRoot(parameters: (any INavigationParameters)?) async throws
    {
        try await self.OnPopToRootAsync(parameters ?? NavigationParameters())
    }

    @MainActor //attribute ensures this method is called in MAIN thread
    public func Navigate(
        name url: String,
        parameters: INavigationParameters? = nil,
        useModalNavigation: Bool = false,
        animated: Bool = true,
        wrapIntoNav: Bool = false
    ) async throws
    {
        let parameters = parameters ?? NavigationParameters()
        let navInfo = UrlNavigationHelper.companion.Parse(url: url)

        if navInfo.isPush
        {
            try await self.OnPushAsync(url, parameters: parameters, animated: animated)
        }
        else if navInfo.isPop
        {
            try await self.OnPopAsync(parameters: parameters)
        }
        else if navInfo.isMultiPop
        {
            try await self.OnMultiPopAsync(url, parameters: parameters, animated: animated)
        }
        else if navInfo.isMultiPopAndPush
        {
            try await self.OnMultiPopAndPush(url, parameters: parameters, animated: animated)
        }
        else if navInfo.isPushAsRoot
        {
            try await self.OnPushRootAsync(url, parameters: parameters, animated: animated)
        }
        else if navInfo.isMultiPushAsRoot
        {
            try await self.OnMultiPushRootAsync(url, parameters: parameters, animated: animated)
        }
        else
        {
            throw NSError(domain: "Navigation", code: -1, userInfo: [NSLocalizedDescriptionKey: "Navigation case is not implemented."])
        }
    }

    private func OnPushAsync(_ vmName: String, parameters: INavigationParameters, animated: Bool) async throws
    {
        let currentToHide = currentPage
        currentPage = NavRegistrar.CreatePage(vmName:vmName, parameters:parameters) as? iOSLifecyclePage
        currentPage?.pushNavAnimated = animated

        currentToHide?.ViewModel.OnNavigatedFrom(parameters: NavigationParameters())
        currentPage?.ViewModel.OnNavigatedTo(parameters:parameters)

        if let page = currentPage
        {
            self.pushViewController(page, animated: animated)
            _ = await PageAppearedAsync(page, animated: animated)
        }
    }

    private func OnPopAsync(parameters: INavigationParameters) async throws
    {
        guard let currentPage = currentPage else { return }
        let animated = currentPage.pushNavAnimated
        let oldPage = currentPage
        let index = self.viewControllers.count - 2
        self.currentPage = self.viewControllers[index] as? iOSLifecyclePage
        self.popViewController(animated: animated)

        oldPage.ViewModel.OnNavigatedFrom(parameters: NavigationParameters())
        self.currentPage?.ViewModel.OnNavigatedTo(parameters:parameters)

        _ = await self.PageDisappearedAsync(oldPage, animated: animated)
        oldPage.Destroy()
    }

    private func OnMultiPopAsync(_ url: String, parameters: INavigationParameters, animated: Bool) async throws
    {
        var arrayToRemove: [iOSLifecyclePage] = []
        var vcs = self.viewControllers //get array copy
        let splitCount = url.split(separator: "/").count - 1

        for _ in 0..<splitCount
        {
            if let page = vcs.last as? iOSLifecyclePage
            {
                vcs.removeLast()
                arrayToRemove.append(page)
            }
        }

        currentPage = vcs.last as? iOSLifecyclePage
        if let currentPage = currentPage
        {
            self.popToViewController(currentPage, animated: animated)
            currentPage.ViewModel.OnNavigatedTo(parameters:parameters)
            _ = await self.PageAppearedAsync(currentPage, animated: animated)

            for removed in arrayToRemove
            {
                removed.Destroy()
            }
        }
    }

    private func OnMultiPopAndPush(_ url: String, parameters: INavigationParameters, animated: Bool) async throws
    {
        var arrayToRemove: [iOSLifecyclePage] = []
        var vcs = self.viewControllers //get array copy
        let splitCount = url.split(separator: "/").count - 1

        for _ in 0..<splitCount
        {
            if let page = vcs.last as? iOSLifecyclePage
            {
                vcs.removeLast()
                arrayToRemove.append(page)
            }
        }

        let vmName = url.replacingOccurrences(of: "../", with: "")
        currentPage = NavRegistrar.CreatePage(vmName: vmName, parameters:parameters) as? iOSLifecyclePage
        currentPage?.pushNavAnimated = animated
        currentPage?.ViewModel.OnNavigatedTo(parameters:parameters)

        if let currentPage = currentPage
        {
            vcs.append(currentPage)
            self.setViewControllers(vcs, animated: animated)
            _ = await self.PageAppearedAsync(currentPage, animated: animated)
        }

        for removed in arrayToRemove
        {
            removed.Destroy()
        }
    }

    private func OnPushRootAsync(_ url: String, parameters: INavigationParameters, animated: Bool) async throws
    {
        let vmName = url
            .replacingOccurrences(of: "/", with: "")
            .replacingOccurrences(of: "NavigationPage", with: "")

        currentPage = NavRegistrar.CreatePage(vmName: vmName, parameters:parameters) as? iOSLifecyclePage
        currentPage?.pushNavAnimated = animated
        currentPage?.ViewModel.OnNavigatedTo(parameters:parameters)

        let vcsToRemove = self.viewControllers.compactMap { $0 as? iOSLifecyclePage }
        self.setViewControllers([currentPage!], animated: animated)
        _ = await self.PageAppearedAsync(currentPage!, animated: animated)

        for vc in vcsToRemove
        {
            vc.Destroy()
        }
    }

    private func OnMultiPushRootAsync(_ url: String, parameters: INavigationParameters, animated: Bool) async throws
    {
        let cleanUrl = url.replacingOccurrences(of: "/NavigationPage", with: "")
        let vmPages = cleanUrl
            .split(separator: "/")
            .map { String($0) }
            .filter { !$0.isEmpty }

        var newPages: [iOSLifecyclePage] = []
        let oldPages = self.viewControllers.compactMap { $0 as? iOSLifecyclePage }

        for vmName in vmPages
        {
            if let page = NavRegistrar.CreatePage(vmName: vmName, parameters:parameters) as? iOSLifecyclePage
            {
                page.pushNavAnimated = animated
                newPages.append(page)
            }
        }

        self.setViewControllers(newPages, animated: animated)
        currentPage = newPages.last
        currentPage?.pushNavAnimated = animated
        currentPage?.ViewModel.OnNavigatedTo(parameters:parameters)

        if let currentPage = currentPage
        {
            _ = await self.PageAppearedAsync(currentPage, animated: animated)
        }

        for oldPage in oldPages
        {
            oldPage.Destroy()
        }
    }

    private func OnPopToRootAsync(_ parameters: INavigationParameters) async throws
    {
        let count = self.viewControllers.count
        if count <= 1
        {
            return
        }
        else if count == 2
        {
            try await self.OnPopAsync(parameters: parameters)
        }
        else
        {
            currentPage = self.viewControllers.first as? iOSLifecyclePage
            let vcsToRemove = self.viewControllers
                .dropFirst()
                .compactMap { $0 as? iOSLifecyclePage }

            self.popToRootViewController(animated: true)
            currentPage?.ViewModel.OnNavigatedTo(parameters:parameters)

            if let currentPage = currentPage
            {
                _ = await self.PageAppearedAsync(currentPage, animated: true)
            }

            for removed in vcsToRemove
            {
                removed.Destroy()
            }
        }
    }
        
    private func PageAppearedAsync(_ page: iOSLifecyclePage, animated: Bool) async -> Bool
    {
        if !animated
        {
            return true
        }

        return await withCheckedContinuation
        {
            continuation in

            var didAppear = false
            var OnAppearingAction: ((Any?) -> Void)!
            
            OnAppearingAction = { [weak page] _ in
                guard let page = page, !didAppear else { return }
                didAppear = true
                page.Appeared.RemoveListener(listener_: OnAppearingAction)
                DispatchQueue.main.async
                {
                    continuation.resume(returning: true)
                }
            }

            page.Appeared.RemoveListener(listener_: OnAppearingAction)
            page.Appeared.AddListener(listener_: OnAppearingAction)
        }
    }
    
    private func PageDisappearedAsync(_ page: iOSLifecyclePage, animated: Bool) async -> Bool
    {
        if !animated
        {
            return true
        }

        return await withCheckedContinuation
        {
            continuation in
            
            var didDisappear = false
            var OnDisappearingAction: ((Any?) -> Void)!
            
            OnDisappearingAction = {[weak page] _ in
                guard let page = page, !didDisappear else { return }
                didDisappear = true
                page.Disappeared.RemoveListener(listener_: OnDisappearingAction)
                
                DispatchQueue.main.async
                {
                    continuation.resume(returning: true)
                }
            }

            page.Disappeared.RemoveListener(listener_: OnDisappearingAction)
            page.Disappeared.AddListener(listener_: OnDisappearingAction)
        }
    }
    
    public func GetCurrentPage() -> (any IPage)?
    {
        let page = self.viewControllers.last as? IPage
        return page
    }
    
    public func GetCurrentPageModel() -> PageViewModel?
    {
        return (self.viewControllers.last as? iOSLifecyclePage)?.ViewModel
    }
    
    public func GetNavStackModels() -> [PageViewModel]
    {
        return self.viewControllers.compactMap { ($0 as? iOSLifecyclePage)?.ViewModel }
    }
    
    public func GetRootPageModel() -> PageViewModel?
    {
        return self.viewControllers
                    .compactMap { ($0 as? iOSLifecyclePage)?.ViewModel }
                    .first
    }
    
}
