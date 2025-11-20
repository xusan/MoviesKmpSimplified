package com.example.movieskmp.base


import com.base.abstractions.Diagnostic.IFileLogger
import com.base.abstractions.Diagnostic.IPlatformOutput
import com.base.abstractions.Diagnostic.SpecificLoggingKeys
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
import com.base.abstractions.Platform.*
import com.base.abstractions.UI.*
import com.base.impl.Droid.Diagnostic.DroidConsoleOutput
import com.base.impl.Droid.Essentials.DroidDirectoryService
import com.base.impl.Droid.Diagnostic.DroidLogbackFileLogger
import com.base.impl.Droid.Essentials.*
import com.base.impl.Droid.PlatformServices.*
import com.base.impl.Droid.UI.*
import org.koin.core.module.Module
import org.koin.dsl.module
import android.os.Handler
import android.os.Looper

class BaseDroidRegistrar
    {
        companion object
        {
            fun RegisterTypes() : List<Module>
            {
                //we need to create the instance of DroidMediaPickerService early as possible otherewise if we try to create instance later it will crash with error:
                // java.lang.IllegalStateException: LifecycleOwner com.example.movieskmp.MainActivity@e0b40f8 is attempting to register while current state is RESUMED.
                val mediaPickerService = DroidMediaPickerService()

                val baseDroidModule = module()
                {
                    //Essentials
                    single<IAppInfo> { DroidAppInfoImplementation() }
                    single<IBrowser> { DroidBrowserImplementation() }
                    single<IDeviceInfo> { DroidDeviceInfoImplementation() }
                    single<IDeviceThreadService> { DroidDeviceThreadService() }
                    single<IDisplay> { DroidDisplayImplementation() }
                    single<IDirectoryService> { DroidDirectoryService() }
                    single<IEmail> { DroidEmailImplementation() }
                    single<IMediaPickerService> { mediaPickerService }
                    single<IPreferences> { DroidPreferencesImplementation() }
                    single<IShare> { DroidShareImplementation() }

                    //PlatformServices
                    single<IZipService> { DroidZipService() }

                    //UI
                    single<IAlertDialogService> { DroidAlertDialogService() }
                    single<ISnackbarService> { DroidSnackbarService() }

                    //Diagnostic
                    single<IFileLogger> { DroidLogbackFileLogger() }
                    single<IPlatformOutput> { DroidConsoleOutput() }
                }

                //we have to delay mediaPickerService initialization
                Handler(Looper.getMainLooper()).post {
                    mediaPickerService.InitSpecificlogger(SpecificLoggingKeys.LogEssentialServices)
                }

                val baseCrossModule = BaseCommonRegistrar.RegisterTypes()

                //merge modules
                val list = baseDroidModule + baseCrossModule;
                return list;
            }
        }

}