package com.base.impl.Droid.UI
import android.app.AlertDialog
import android.content.DialogInterface
import com.base.abstractions.Diagnostic.SpecificLoggingKeys
import com.base.abstractions.UI.IAlertDialogService
import com.base.impl.Diagnostic.LoggableService
import com.base.impl.Droid.Utils.CurrentActivity
import com.base.impl.UI.ActionSheetArguments
import com.base.impl.UI.AlertArguments
import kotlinx.coroutines.*

internal class DroidAlertDialogService : LoggableService(), IAlertDialogService
{
    init
    {
        InitSpecificlogger(SpecificLoggingKeys.LogUIServices)
    }

    override suspend fun ConfirmAlert(title: String, message: String, vararg buttons: String): Boolean
    {
        SpecificLogMethodStart(::ConfirmAlert.name, title, message, buttons)
        val accept = buttons.elementAtOrNull(0)
        val cancel = buttons.elementAtOrNull(1)
        val arguments = AlertArguments(title, message, accept, cancel)

        this.PostExecuteAlert(arguments)

        return arguments.Result.await()
    }

    override suspend fun DisplayAlert(title: String, message: String, cancel: String)
    {
        SpecificLogMethodStart(::DisplayAlert.name, title, message, cancel)

        val arguments = AlertArguments(title, message, null, cancel)
        this.PostExecuteAlert(arguments)

        arguments.Result.await()
    }

    override suspend fun DisplayActionSheet(title: String, vararg buttons: String): String?
    {
        return this.DisplayActionSheet(title, null, null, *buttons)
    }

    override suspend fun DisplayActionSheet(title: String, cancel: String?, destruction: String?, vararg buttons: String): String?
    {
        SpecificLogMethodStart("DisplayActionSheet", title, cancel, destruction, buttons)
        val arguments = ActionSheetArguments(title, cancel, destruction, buttons.toList())

        this.PostExecuteAlertSheet(arguments)

        return arguments.Result.await()
    }

    private fun PostExecuteAlert(arguments: AlertArguments)
    {
        SpecificLogMethodStart(::PostExecuteAlert.name)
        CoroutineScope(Dispatchers.Main).launch {
            delay(10)

            val alert = AlertDialog.Builder(CurrentActivity.Instance).create()
            alert.setTitle(arguments.Title)
            alert.setMessage(arguments.Message)
            if (arguments.Accept != null)
            {
                alert.setButton(DialogInterface.BUTTON_POSITIVE, arguments.Accept) { _, _ -> arguments.SetResult(true) }
            }
            alert.setButton(DialogInterface.BUTTON_NEGATIVE, arguments.Cancel) { _, _ -> arguments.SetResult(false) }
            alert.setOnCancelListener { arguments.SetResult(false) }
            alert.show()
        }
    }

    private fun PostExecuteAlertSheet(arguments: ActionSheetArguments)
    {
        SpecificLogMethodStart(::PostExecuteAlertSheet.name)
        CoroutineScope(Dispatchers.Main).launch {
            delay(10)

            val builder = AlertDialog.Builder(CurrentActivity.Instance)
            builder.setTitle(arguments.Title)
            val items = arguments.Buttons.toTypedArray()
            builder.setItems(items) { _, which -> arguments.SetResult(items[which]) }

            if (arguments.Cancel != null)
                builder.setPositiveButton(arguments.Cancel) { _, _ -> arguments.SetResult(arguments.Cancel) }

            if (arguments.Destruction != null)
                builder.setNegativeButton(arguments.Destruction) { _, _ -> arguments.SetResult(arguments.Destruction) }

            val dialog = builder.create()

            dialog.setCanceledOnTouchOutside(true)
            dialog.setOnCancelListener { arguments.SetResult(null) }
            dialog.show()
        }
    }
}




