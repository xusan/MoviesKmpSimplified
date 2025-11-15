package com.base.abstractions.UI

import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

@OptIn(ExperimentalObjCName::class)
@ObjCName(name = "IAlertDialogService", exact = true)
interface IAlertDialogService
{
    suspend fun DisplayAlert(title: String, message: String, cancel: String = "Close")
    suspend fun ConfirmAlert(title: String, message: String, vararg buttons: String): Boolean

    suspend fun DisplayActionSheet(title: String, vararg buttons: String): String?
    suspend fun DisplayActionSheet(title: String, cancel: String?, destruction: String?, vararg buttons: String): String?
}


