package com.base.mvvm.Droid.Navigation.Pages


import android.os.Bundle
import android.view.MotionEvent
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.base.abstractions.Diagnostic.ILoggingService
import com.base.impl.ContainerLocator
import com.base.impl.Droid.Utils.ContextExtensions.HideKeyboard
import com.base.impl.Droid.Utils.ContextExtensions.ToPixels
import com.base.impl.Droid.Utils.CurrentActivity
import com.base.abstractions.Common.Point
import com.base.abstractions.Common.Rectangle
import com.base.mvvm.Navigation.IPage
import com.base.mvvm.ViewModels.PageViewModel
import java.util.*


//import com.base.impl.Utils.*

open class DroidBasePage : Fragment(), IPage, IDispatchEventListener {
    override lateinit var ViewModel: PageViewModel

    protected lateinit var loggingService: ILoggingService

    private var downPosition: Point? = null
    private var downTime: Date? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loggingService = ContainerLocator.Resolve<ILoggingService>()
    }

    /**
     * When user click on page we should hide keyboard
     */
    override fun DispatchTouchEvent(e: MotionEvent) {
        if (e.action == MotionEvent.ACTION_DOWN) {
            downTime = Date()
            downPosition = Point(e.rawX.toDouble(), e.rawY.toDouble())
        }

        if (e.action != MotionEvent.ACTION_UP)
            return

        val currentView = CurrentActivity.Instance.currentFocus

        if (currentView !is EditText)
            return

        val newCurrentView = CurrentActivity.Instance.currentFocus

        if (currentView != newCurrentView)
            return

        val ctx = context;
        if (ctx != null) {
            val distance = downPosition?.Distance(Point(e.rawX.toDouble(), e.rawY.toDouble()))

            if (distance != null) {
                if (distance > ctx.ToPixels(20.0).toDouble() || Date().time - downTime!!.time > 200)
                    return
            }

            val location = IntArray(2)
            currentView.getLocationOnScreen(location)

            val x = e.rawX + currentView.left - location[0]
            val y = e.rawY + currentView.top - location[1]

            val rect = Rectangle(
                currentView.left.toDouble(),
                currentView.top.toDouble(),
                currentView.width.toDouble(),
                currentView.height.toDouble()
            )

            if (rect.Contains(x.toDouble(), y.toDouble()))
                return

            ctx.HideKeyboard(currentView)
            CurrentActivity.Instance.window.decorView.clearFocus()
        }
    }
}



