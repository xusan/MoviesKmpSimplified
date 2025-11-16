import SharedAppCore
import UIKit

extension XfColor
{
    func ToUIColor() -> UIColor
    {
        return UIColor(
            red: CGFloat(R),
            green: CGFloat(G),
            blue: CGFloat(B),
            alpha: CGFloat(A)
        )
    }
}

public extension UIColor
{
    func MakeDarker(_ percent: CGFloat) -> UIColor
    {
        let helper = ColorHelper(color: self)
        return helper.Darker(percent)
    }

    func MakeLighter(_ percent: CGFloat) -> UIColor
    {
        let helper = ColorHelper(color: self)
        return helper.Lighter(percent)
    }
}
