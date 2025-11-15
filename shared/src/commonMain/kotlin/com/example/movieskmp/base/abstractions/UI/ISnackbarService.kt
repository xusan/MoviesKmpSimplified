package com.base.abstractions.UI

import com.base.abstractions.Event
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

@OptIn(ExperimentalObjCName::class)
@ObjCName(name = "ISnackbarService", exact = true)
interface ISnackbarService
{
    val PopupShowed: Event<SeverityType>
    fun ShowError(message: String);
    fun ShowInfo(message: String);
    fun Show(message: String, severityType: SeverityType, duration: Int = 3000);
}

@OptIn(ExperimentalObjCName::class)
@ObjCName(name = "SeverityType", exact = true)
enum class SeverityType
{
    Info,
    Success,
    Warning,
    Error
}