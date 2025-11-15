package com.base.mvvm.Helpers

import kotlinx.datetime.Clock

class ClickUtil
{
    companion object
    {
        const val OneClickDelay: Int = 1000;
    }
    private var lastClickTime: Long = 0

    fun isOneClick(): Boolean
    {
        val clickTime = Clock.System.now().toEpochMilliseconds()
        if (clickTime - lastClickTime < OneClickDelay)
            return false

        lastClickTime = clickTime
        return true
    }
}