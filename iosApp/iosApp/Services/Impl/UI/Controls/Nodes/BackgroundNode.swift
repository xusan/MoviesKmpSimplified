import AsyncDisplayKit
import UIKit

@objcMembers
public class BackgroundNode : BaseControlNode
{
    //public var isMyMessage: Bool?
    
    @objc public convenience init(normalColor: UIColor)
    {
        self.init(
            normalColor: normalColor,
            pressedColor: ColorConstants.Gray100.ToUIColor()
        )
    }

    @objc public convenience init(normalColor: UIColor, pressedColor: UIColor)
    {
        self.init(
            normalColor: normalColor,
            pressedColor: pressedColor,
            selectedColor: pressedColor,
            isSelected: false
        )
    }

    @objc public override init(normalColor: UIColor, pressedColor: UIColor, selectedColor: UIColor, isSelected: Bool)
    {
        super.init(
            normalColor: normalColor,
            pressedColor: pressedColor,
            selectedColor: selectedColor,
            isSelected: isSelected
        )
    }

       

    public override func didLoad()
    {
        super.didLoad()

        self.clipsToBounds = true

//        if let isMyMessage = self.isMyMessage
//        {
//            self.view.layer.maskedCorners = [.minXMinYCorner]
//
//            if isMyMessage
//            {
//                self.view.layer.maskedCorners = [.minXMinYCorner, .minXMaxYCorner, .maxXMinYCorner]
//            }
//            else
//            {
//                self.view.layer.maskedCorners = [.minXMinYCorner, .maxXMaxYCorner, .maxXMinYCorner]
//            }
//        }
    }
}
