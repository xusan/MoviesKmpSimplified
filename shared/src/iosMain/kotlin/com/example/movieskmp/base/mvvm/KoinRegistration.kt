import com.base.abstractions.Diagnostic.IErrorTrackingService
import com.base.abstractions.Diagnostic.IFileLogger
import com.base.abstractions.Diagnostic.ILoggingService
import com.base.abstractions.Diagnostic.IPlatformOutput
import com.base.abstractions.Essentials.Browser.IBrowser
import com.base.abstractions.Essentials.Device.IDeviceInfo
import com.base.abstractions.Essentials.Display.IDisplay
import com.base.abstractions.Essentials.Email.IEmail
import com.base.abstractions.Essentials.IAppInfo
import com.base.abstractions.Essentials.IDeviceThreadService
import com.base.abstractions.Essentials.IDirectoryService
import com.base.abstractions.Essentials.IMediaPickerService
import com.base.abstractions.Essentials.IPreferences
import com.base.abstractions.Essentials.IShare
import com.base.abstractions.IConstant
import com.base.abstractions.Platform.IZipService
import com.base.abstractions.UI.IAlertDialogService
import com.base.abstractions.UI.ISnackbarService
import com.base.mvvm.Navigation.IPageNavigationService
import com.example.movieskmp.base.BaseiOSRegistrar
import org.koin.core.context.startKoin
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module

/**
 * Initializes Koin dependency injection for iOS platform.
 *
 * This function should be called once during app initialization to set up the DI container.
 * It registers cross-platform implementations and iOS-specific services.
 */
fun StartKoinForIOS()
{
    // Register cross-platform implementation modules
    val mergedModules = com.example.movieskmp.AppiOSRegistrar.RegisterTypes()

    startKoin()
    {
        modules(mergedModules)
    }
}


/**
 * Registers iOS-specific service implementations with Koin.
 *
 * This function should be called from iOS Swift code to provide platform-specific implementations
 * for various services. All parameters are optional - only provide factories for services that need
 * iOS-specific implementations.
 *
 * @param appInfoFactory Factory for providing app information (version, name, etc.)
 * @param prefereceFactory Factory for preferences/user defaults storage
 * @param browserFactory Factory for opening web browsers
 * @param deviceInfoFactory Factory for device information (model, OS version, etc.)
 * @param deviceThreadFactory Factory for managing threads/dispatchers
 * @param dsplayFactory Factory for display information (screen size, orientation, etc.)
 * @param directoryFactory Factory for file system directory operations
 * @param emailServiceFactory Factory for email composition
 * @param shareFactory Factory for system share sheet
 * @param keyboardResizeFactory Factory for keyboard resize behavior management
 * @param zipServiceFactory Factory for zip/compression operations
 * @param snackbarFactory Factory for displaying snackbar notifications
 * @param alertDialogFactory Factory for displaying alert dialogs
 * @param mediaPickerFactory Factory for photo/media picker
 * @param outputFactory Factory for platform-specific logging output
 * @param fileLoggerFactory Factory for file-based logging
 */
fun RegisterIOSService(
    //Common
    constantsService: (() -> IConstant)? = null,
    //Navigation
    navigationService: (() -> IPageNavigationService)? = null,
    //Essentials
    appInfoFactory: (() -> IAppInfo)? = null,
    prefereceFactory: (() -> IPreferences)? = null,
    browserFactory: (() -> IBrowser)? = null,
    deviceInfoFactory: (() -> IDeviceInfo)? = null,
    deviceThreadFactory: (() -> IDeviceThreadService)? = null,
    displayFactory: (() -> IDisplay)? = null,
    directoryFactory: (() -> IDirectoryService)? = null,
    emailServiceFactory: (() -> IEmail)? = null,
    shareFactory: (() -> IShare)? = null,
    zipServiceFactory: (() -> IZipService)? = null,
    //UI
    snackbarFactory: (() -> ISnackbarService)? = null,
    alertDialogFactory: (() -> IAlertDialogService)? = null,
    mediaPickerFactory: (() -> IMediaPickerService)? = null,
    //Diagnostic
    outputFactory: (() -> IPlatformOutput)? = null,
    fileLoggerFactory: (() -> IFileLogger)? = null,
    errorTracking: (() -> IErrorTrackingService)? = null,
)
{
    val iosModule = module()
    {
        //Common
        constantsService?.let { single<IConstant> { it() } }
        //Navigation
        navigationService?.let { single<IPageNavigationService> { it() } }
        // Register Essentials services
        appInfoFactory?.let { single<IAppInfo> { it() } }
        prefereceFactory?.let { single<IPreferences> { it() } }
        browserFactory?.let { single<IBrowser> { it() } }
        deviceInfoFactory?.let { single<IDeviceInfo> { it() } }
        deviceThreadFactory?.let { single<IDeviceThreadService> { it() } }
        displayFactory?.let { single<IDisplay> { it() } }
        directoryFactory?.let { single<IDirectoryService> { it() } }
        emailServiceFactory?.let { single<IEmail> { it() } }
        shareFactory?.let { single<IShare> { it() } }
        zipServiceFactory?.let { single<IZipService> { it() } }
        // Register UI services
        alertDialogFactory?.let { single<IAlertDialogService> { it() } }
        snackbarFactory?.let { single<ISnackbarService> { it() } }
        mediaPickerFactory?.let { single<IMediaPickerService> { it() } }
        // Register Diagnostic services
        outputFactory?.let { single<IPlatformOutput> { it() } }
        fileLoggerFactory?.let { single<IFileLogger> { it() } }
        errorTracking?.let { single<IErrorTrackingService> { it() } }
    }

    // Load the iOS module into Koin
    loadKoinModules(iosModule)
}





