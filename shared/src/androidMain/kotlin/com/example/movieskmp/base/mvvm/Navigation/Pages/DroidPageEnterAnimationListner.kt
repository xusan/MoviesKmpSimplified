package com.base.mvvm.Droid.Navigation.Pages

import android.view.animation.Animation
import com.base.impl.Droid.Utils.CurrentActivity

class DroidPageEnterAnimationListner(
    private val page: DroidLifecyclePage
) : Animation.AnimationListener {

    override fun onAnimationEnd(animation: Animation?)
    {
        CurrentActivity.Instance.runOnUiThread()
        {
            page.OnPageEnterAnimationCompleted()
        }
    }

    override fun onAnimationRepeat(animation: Animation?) {

    }

    override fun onAnimationStart(animation: Animation?) {

    }
}