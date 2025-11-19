package com.base.impl.Droid.UI

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import com.base.abstractions.Diagnostic.SpecificLoggingKeys
import com.base.abstractions.Event
import com.base.abstractions.UI.ISnackbarService
import com.base.abstractions.UI.SeverityType
import com.base.impl.Diagnostic.LoggableService
import com.base.impl.Droid.Utils.CurrentActivity
import com.base.impl.Droid.Utils.ToAndroid
import com.base.impl.UI.SnackbarColors.GetBackgroundColor
import com.base.impl.UI.SnackbarColors.GetTextColor
import com.example.movieskmp.shared.R

internal class DroidSnackbarService : LoggableService(), ISnackbarService
{
    init
    {
        InitSpecificlogger(SpecificLoggingKeys.LogUIServices)
    }

    private lateinit var rootView: ViewGroup
    private lateinit var snackbarView: View
    private var isVisible: Boolean = false

    override val PopupShowed = Event<SeverityType>()

    override fun ShowError(message: String)
    {
        this.Show(message, SeverityType.Error)
    }

    override fun ShowInfo(message: String)
    {
        this.Show(message, SeverityType.Info)
    }

    override fun Show(message: String, severityType: SeverityType, duration: Int)
    {
        SpecificLogMethodStart("Show", message, severityType, duration)
        val activity = CurrentActivity.Instance
        val inflater = LayoutInflater.from(activity)

        var content = activity.window.decorView.findViewById<ViewGroup>(android.R.id.content)
        this.rootView = content.getChildAt(0) as ViewGroup
        snackbarView = inflater.inflate(R.layout.custom_snackbar, rootView, false)

        if (rootView !is FrameLayout)
        {
            throw Exception("To ensure the Snackbar works correctly, it's recommended to use a FrameLayout as the Activity's root view.")
        }

        snackbarView.setBackgroundColor(severityType.GetBackgroundColor().ToAndroid())

        val textView = snackbarView.findViewById<TextView>(R.id.snackbar_text)
        textView.text = message
        textView.setTextColor(severityType.GetTextColor().ToAndroid())

        rootView.addView(snackbarView)

        // Make sure it's measured
        snackbarView.post {
            snackbarView.translationY = GetTranslateY(snackbarView) // hide it initially
        }

        snackbarView.post {
            snackbarView.animate()
                .translationY(0f)
                .setDuration(300)
                .start()

            isVisible = true

            // Auto hide after duration
            snackbarView.postDelayed({
                if (isVisible)
                    Hide()
            }, duration.toLong())
        }
    }

    fun Hide()
    {
        SpecificLogMethodStart(::Hide.name)
        snackbarView.post {
            val hideY = GetTranslateY(snackbarView)
            snackbarView.animate()
                .translationY(hideY)
                .setDuration(300)
                .withEndAction {
                    rootView.removeView(snackbarView)
                }
                .start()

            isVisible = false
        }
    }

    private fun GetTranslateY(view: View): Float
    {
        SpecificLogMethodStart(::GetTranslateY.name)
        val hideY = -(view.height + GetTopMargin(snackbarView))

        return hideY
    }

    private fun GetTopMargin(view: View): Float
    {
        SpecificLogMethodStart(::GetTopMargin.name)
        if (view.layoutParams is ViewGroup.MarginLayoutParams)
        {
            val marginParams = view.layoutParams as ViewGroup.MarginLayoutParams
            return marginParams.topMargin.toFloat()
        }
        return 0f
    }

}