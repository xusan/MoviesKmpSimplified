import UIKit
import AsyncDisplayKit

public final class ButtonStyles {
    
    public static func CreateIconButton(_ icon: String) -> IconButtonNode {
        let iconBtn = IconButtonNode(
            normalColor: UIColor.white,
            pressedColor: ColorConstants.PrimaryColor2.ToUIColor(),
            icon: icon
        )
        return iconBtn
    }
    
    // Uncomment when TabButtonNode is implemented
    /*
    public static func CreateTabButton(_ icon: String, iconSelected: String, text: String, isSelected: Bool = false) -> TabButtonNode {
        let iconBtn = TabButtonNode(icon: icon, iconSelected: iconSelected, text: text, isSelected: isSelected)
        iconBtn.style.flexGrow = 1
        iconBtn.style.width = ASDimension(unit: .fraction, value: 0.25)
        return iconBtn
    }
    */
    
    public static func CreatePrimaryButton(_ text: String) -> ButtonNode {
        let txtAttr: [NSAttributedString.Key: Any] = [
            .font: UIFont(name: "Sen-Bold", size: 18)!,
            .foregroundColor: UIColor.white
        ]
        
        let btn = ButtonNode(normalColor: ColorConstants.PrimaryColor.ToUIColor(), pressedColor: ColorConstants.PrimaryDark.ToUIColor())
        btn.cornerRadius = CGFloat(NumConstants.BtnHeight / 2)
        btn.setAttributedTitle(NSAttributedString(string: text, attributes: txtAttr), for: UIControl.State.normal)
        
        btn.style.preferredLayoutSize = ASLayoutSize(
            width: ASDimension(unit: .auto, value: 0),
            height: ASDimension(unit: .points, value: CGFloat(NumConstants.BtnHeight))
        )
        
        return btn
    }
    
    // Uncomment when implementing secondary button
    /*
    public static func CreateSecondaryButton(_ text: String) -> ButtonNode {
        let btn = CreatePrimaryButton(text)
        btn.normalColor = UIColorConstants.LabelColor
        btn.pressedColor = UIColorConstants.LabelColor.makeDarker(0.5)
        btn.backgroundColor = btn.normalColor
        return btn
    }
    */
}
