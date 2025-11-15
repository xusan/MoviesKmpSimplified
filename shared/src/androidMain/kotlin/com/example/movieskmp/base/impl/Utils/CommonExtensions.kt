package com.base.impl.Droid.Utils


import android.view.View

fun Boolean.ToVisibility(makeGone: Boolean = false): Int
{
    if (this)
    {
        return View.VISIBLE
    }
    else
    {
        if (makeGone)
        {
            return View.GONE
        }
        else
        {
            return View.INVISIBLE
        }
    }
}

fun Boolean.ToNotVisibility(makeGone: Boolean = false): Int
{
    if (this)
    {
        if (makeGone) {
            return View.GONE
        } else {
            return View.INVISIBLE
        }
    } else
    {
        return View.VISIBLE
    }
}


