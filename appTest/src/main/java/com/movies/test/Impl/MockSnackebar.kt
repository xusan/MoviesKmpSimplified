package com.movies.test.Impl

import com.base.abstractions.Event
import com.base.abstractions.UI.ISnackbarService
import com.base.abstractions.UI.SeverityType

class MockSnackebar : ISnackbarService
{
    override val PopupShowed = Event<SeverityType>()

    override fun ShowError(message: String)
    {
        PopupShowed.Invoke(SeverityType.Error)
    }

    override fun ShowInfo(message: String)
    {
        PopupShowed.Invoke(SeverityType.Info)
    }

    override fun Show(message: String, severityType: SeverityType, duration: Int)
    {
        PopupShowed.Invoke(SeverityType.Error)
    }
}