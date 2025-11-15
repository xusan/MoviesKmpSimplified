package com.base.impl.UI

import kotlinx.coroutines.CompletableDeferred

class AlertArguments
{
    /**
     * Gets the text for the accept button. Can be null.
     */
    var Accept: String? private set

    /**
     * Gets the text of the cancel button.
     */
    var Cancel: String? private set

    /**
     * Gets the message for the alert. Can be null.
     */
    var Message: String? private set

    val Result: CompletableDeferred<Boolean>

    /**
     * Gets the title for the alert. Can be null.
     */
    var Title: String? private set

    constructor(title: String?, message: String?, accept: String?, cancel: String?)
    {
        Title = title
        Message = message
        Accept = accept
        Cancel = cancel
        Result = CompletableDeferred()
    }

    fun SetResult(result: Boolean)
    {
        Result.complete(result)
    }
}

