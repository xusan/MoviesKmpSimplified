package com.base.mvvm.Navigation

class UrlNavigationHelper
{
    var isPop: Boolean = false
    var isMultiPop: Boolean = false
    var isMultiPopAndPush: Boolean = false
    var isPush: Boolean = false
    var isPushAsRoot: Boolean = false
    var isMultiPushAsRoot: Boolean = false

    companion object
    {
        fun Parse(url: String): UrlNavigationHelper
        {
            val obj = UrlNavigationHelper()

            if (url == "../")
            {
                obj.isPop = true
            }
            else if (url.contains("../"))
            {
                obj.isMultiPop = url.replace("../", "") == ""
                obj.isMultiPopAndPush = !obj.isMultiPop
            }
            else if (url.contains("/"))
            {
                val pages = url.split("/").filter { it.isNotEmpty() }
                if (pages.size > 1)
                {
                    obj.isMultiPushAsRoot = true
                }
                else
                {
                    obj.isPushAsRoot = true
                }
            }
            else
            {
                obj.isPush = true
            }

            return obj
        }
    }
}