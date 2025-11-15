package com.base.mvvm.Helpers

import com.base.abstractions.BaseEvent
import com.base.abstractions.Diagnostic.ILoggingService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class AsyncCommand(
    private val scope: CoroutineScope,
    private val executeFunc: suspend (Any?) -> Unit,
    private val canExecuteFunc: ((Any?) -> Boolean)? = null
)
{
    constructor(scope: CoroutineScope, executeFunc: suspend (Any?) -> Unit) : this(scope, executeFunc, { true })
    companion object
    {
        var DisableDoubleClickCheck: Boolean = false;
        var loggingService: ILoggingService? = null
    }

    val doubleClickChecker: ClickUtil = ClickUtil()
    val CanExecuteChanged: BaseEvent = BaseEvent()

    fun CanExecute(param: Any?): Boolean = canExecuteFunc?.invoke(param) ?: true


    suspend fun ExecuteAsync(param: Any? = null)
    {
        if (DisableDoubleClickCheck == false)
        {
            if (!doubleClickChecker.isOneClick())
            {
                loggingService?.LogWarning("AsyncCommand.ExecuteAsync() is ignored because it is not permitted to execute second click withtin ${ClickUtil.OneClickDelay}mls");
                return
            }
        }

        executeFunc(param);
    }

    fun RaiseCanExecuteChanged()
    {
        CanExecuteChanged.Invoke()
    }

    fun Execute(param: Any?)
    {
        scope.launch()
        {
            ExecuteAsync(param)
        }
    }

    fun Execute()
    {
        Execute(null)
    }

}