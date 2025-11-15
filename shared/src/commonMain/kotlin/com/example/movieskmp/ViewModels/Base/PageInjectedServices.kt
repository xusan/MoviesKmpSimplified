package com.app.shared.Base

import com.base.abstractions.Essentials.Device.IDeviceInfo
//import com.base.abstractions.Platform.IDeviceService
import com.base.abstractions.UI.IAlertDialogService
import com.base.mvvm.ViewModels.InjectedService
import org.koin.core.component.inject

class PageInjectedServices : InjectedService()
{
    val DeviceService: IDeviceInfo by inject()
    val AlertDialogService: IAlertDialogService by inject()
}