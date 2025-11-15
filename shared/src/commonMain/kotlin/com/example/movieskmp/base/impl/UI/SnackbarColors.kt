package com.base.impl.UI

import com.base.abstractions.Common.XfColor
import com.base.abstractions.UI.SeverityType

object SnackbarColors
{
    var InfoColor: XfColor = XfColor.FromHex("#E1F0FF")
    var InfoTextColor: XfColor = XfColor.FromHex("#FFF9F9FA")

    var ErrorColor: XfColor = XfColor.FromHex("#FFEAEB")
    var ErrorTextColor: XfColor = XfColor.FromHex("#ff4444")

    var SuccessColor: XfColor = XfColor.FromHex("#FFCDFFCD")
    var SuccessTextColor: XfColor = XfColor.FromHex("#FF114338")

    fun SeverityType.GetBackgroundColor(): XfColor
    {
        if (this == SeverityType.Info)
            return InfoColor
        else if (this == SeverityType.Error || this == SeverityType.Warning)
            return ErrorColor
        else
            return SuccessColor
    }

    fun SeverityType.GetTextColor(): XfColor
    {
        if (this == SeverityType.Info)
            return InfoTextColor
        else if (this == SeverityType.Error || this == SeverityType.Warning)
            return ErrorTextColor
        else
            return SuccessTextColor
    }
}
