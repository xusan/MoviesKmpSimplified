import SharedAppCore

class Bootstrap
{    
    func NavigateToPage(_ navigationService: IPageNavigationService) async
    {
        let preferences = try! KoinResolver().GetPreferences()
        let isLoggedIn = preferences.Get(LoginPageViewModel.companion.IsLoggedIn, default: false)

        if isLoggedIn
        {
            let mainPage = String(describing: MoviesPageViewModel.self)
            try! await navigationService.Navigate(name: "/\(mainPage)", parameters: nil, useModalNavigation: false, animated: false, wrapIntoNav: false)
        }
        else
        {
            let loginPage = String(describing: LoginPageViewModel.self)
            try! await navigationService.Navigate(name: "/\(loginPage)", parameters: nil, useModalNavigation: false, animated: false, wrapIntoNav: false)
        }
    }
    
   
    @MainActor
    func RegisterTypes(_ navService: IPageNavigationService, _ errorTrackingService: IErrorTrackingService)
    {
        //start DI
        KoinRegistrationKt.StartKoinForIOS()

        //Register required services:
        KoinRegistrationKt.RegisterIOSService(
            constantsService: { ConstantImpl() },
            navigationService: { navService },
            appInfoFactory: {IOSAppInfoImplementation() },
            prefereceFactory: { iOSPreferencesImplementation() },
            browserFactory: { IOSBrowserImplementation() },
            deviceInfoFactory: { iOSDeviceInfoImplementation() },
            deviceThreadFactory: { iOSDeviceThreadService() },
            displayFactory: { iOSDisplayImplementation() },
            directoryFactory: { iOSDirectoryService() },
            emailServiceFactory: nil,
            shareFactory: { iOSShareImplementation() },
            zipServiceFactory: { iOSZipService() },
            snackbarFactory: { iOSSnackbarBarService() },
            alertDialogFactory: { iOSAlertDialogService() },
            mediaPickerFactory: { iOSMediaPickerService() },
            outputFactory: { iOSConsoleOutput() },
            fileLoggerFactory: { iOSFileLogger() },
            errorTracking : { errorTrackingService }
        )

        //Register pages, viewmodels mapping
        let services = PageInjectedServices()
        NavRegistrar.RegisterPageForNavigation({ LoginPage() }, { LoginPageViewModel(injectedService: services) })
        NavRegistrar.RegisterPageForNavigation({ MoviesPage() }, { MoviesPageViewModel(injectedService: services) })
        NavRegistrar.RegisterPageForNavigation({ MovieDetailPage() }, { MovieDetailPageViewModel(injectedService: services) })
        NavRegistrar.RegisterPageForNavigation({ AddEditMoviePage() }, { AddEditMoviePageViewModel(injectedService: services) })
    }
}


//highlight shared classes in xcode editor
public typealias PageViewModel = SharedAppCore.PageViewModel
public typealias IPage = SharedAppCore.IPage
typealias AppPageViewModel = SharedAppCore.AppPageViewModel
typealias LoginPageViewModel = SharedAppCore.LoginPageViewModel
typealias MoviesPageViewModel = SharedAppCore.MoviesPageViewModel
typealias MovieDetailPageViewModel = SharedAppCore.MovieDetailPageViewModel
typealias AddEditMoviePageViewModel = SharedAppCore.AddEditMoviePageViewModel
typealias PageInjectedServices = SharedAppCore.PageInjectedServices
typealias IPageNavigationService = SharedAppCore.IPageNavigationService
typealias NavigationParameters = SharedAppCore.NavigationParameters
typealias UrlNavigationHelper = SharedAppCore.UrlNavigationHelper
typealias KoinResolver = SharedAppCore.KoinResolver
typealias KoinRegistrationKt = SharedAppCore.KoinRegistrationKt
typealias ILoggingService = SharedAppCore.ILoggingService
typealias IFileLogger = SharedAppCore.IFileLogger
typealias IPlatformOutput = SharedAppCore.IPlatformOutput
typealias MediaOptions = SharedAppCore.MediaOptions
typealias MediaFile = SharedAppCore.MediaFile
typealias MediaSource = SharedAppCore.MediaSource
typealias IMediaPickerService = SharedAppCore.IMediaPickerService
public typealias XfColor = SharedAppCore.XfColor
