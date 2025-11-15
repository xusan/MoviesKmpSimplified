package com.base.impl.UI

import kotlinx.coroutines.CompletableDeferred

class ActionSheetArguments(title: String, cancel: String?, destruction: String?, buttons: Iterable<String>?)
{
    /**
     * Gets titles of any buttons on the action sheet that aren't [Cancel] or [Destruction]. Can
     * be `null`.
     */
    var Buttons: List<String> = buttons?.filter { it != null } ?: emptyList()
        private set

    /**
     * Gets the text for a cancel button. Can be null.
     */
    var Cancel: String? = cancel
        private set

    /**
     * Gets the text for a destructive button. Can be null.
     */
    var Destruction: String? = destruction
        private set

    val Result: CompletableDeferred<String?> = CompletableDeferred()

    /**
     * Gets the title for the action sheet. Can be null.
     */
    var Title: String = title
        private set

    fun SetResult(result: String?)
    {
        Result.complete(result)
    }
}