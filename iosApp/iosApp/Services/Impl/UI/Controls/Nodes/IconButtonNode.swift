import AsyncDisplayKit
import UIKit

public class IconButtonNode : BackgroundNode
{
    internal let iconPadding: Int
    internal var svgNode: SvgViewNode

    // MARK: - Initializer

    public init(normalColor: UIColor, pressedColor: UIColor, icon: String, iconSize: Int = 26, iconPadding: Int = 12)
    {
        self.iconPadding = iconPadding
        self.svgNode = SvgViewNode(svgFile: icon)
        
        // ✅ Call the designated initializer of the superclass
        super.init(normalColor: normalColor,
                    pressedColor: pressedColor,
                    selectedColor: pressedColor,
                    isSelected: false)
        
        self.backgroundColor = normalColor
        self.cornerRadius = CGFloat(iconSize + (2 * iconPadding)) / 2.0

        self.svgNode.style.preferredSize = CGSize(width: iconSize, height: iconSize)
    }

    // MARK: - Layout

    public override func layoutSpecThatFits(_ constrainedSize: ASSizeRange) -> ASLayoutSpec
    {
        let inset = ASInsetLayoutSpec()
        inset.insets = UIEdgeInsets(top: CGFloat(iconPadding),
                                    left: CGFloat(iconPadding),
                                    bottom: CGFloat(iconPadding),
                                    right: CGFloat(iconPadding))
        inset.child = svgNode

        return inset
    }
}
