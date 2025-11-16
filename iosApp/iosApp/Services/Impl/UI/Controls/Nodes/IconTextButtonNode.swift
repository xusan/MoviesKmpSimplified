import AsyncDisplayKit
import UIKit

public class IconTextButtonNode : BackgroundNode
{
    private var svgNode: SvgViewNode?
    private var txtNode: ASTextNode?
    private var spacing: Int = 0
    private var hPadding: Int = 0
    private var hAligment: ASStackLayoutJustifyContent = .center
    private let height: Int = 60

    public var IsLayoutVertical: Bool = false

    // MARK: - Initializers

    public convenience init(normalColor: UIColor)
    {
        self.init(
            normalColor: normalColor,
            pressedColor: ColorConstants.Gray100.ToUIColor()
        )
    }

    public convenience init(normalColor: UIColor, pressedColor: UIColor)
    {
        self.init(normalColor: normalColor,
                  pressedColor: pressedColor,
                  selectedColor: pressedColor,
                  isSelected: false)
    }

    public override init(normalColor: UIColor, pressedColor: UIColor, selectedColor: UIColor, isSelected: Bool)
    {
        super.init(normalColor: normalColor, pressedColor: pressedColor, selectedColor: selectedColor, isSelected: isSelected)
    }

    // MARK: - Setup

    public func SetIconText(
        _ icon: String,
        _ text: String,
        spacing: Int = 8,
        iconSize: Int = 34,
        textColor: UIColor? = nil,
        textFont: UIFont? = nil,
        hPadding: Int = 15,
        hAligment: ASStackLayoutJustifyContent = .center,
        corner: Int? = nil)
    {
        let resolvedTextColor = textColor ?? ColorConstants.DefaultTextColor.ToUIColor()
        let resolvedFont = textFont ?? UIFont(name: "Sen-Bold", size: 20)!

        if corner == nil
        {
            self.cornerRadius = CGFloat(height) / 2.0
        }
        else
        {
            self.cornerRadius = 0
        }

        self.spacing = spacing
        self.hPadding = hPadding
        self.hAligment = hAligment

        let svgNode = SvgViewNode(svgFile: icon)
        svgNode.style.preferredSize = CGSize(width: iconSize, height: iconSize)
        self.svgNode = svgNode

        let attributes: [NSAttributedString.Key: Any] = [
            .font: resolvedFont,
            .foregroundColor: resolvedTextColor
        ]

        let txtNode = ASTextNode()
        txtNode.attributedText = NSAttributedString(string: text, attributes: attributes)
        self.txtNode = txtNode
    }

    // MARK: - Layout

    public override func layoutSpecThatFits(_ constrainedSize: ASSizeRange) -> ASLayoutSpec
    {
        guard let svgNode = self.svgNode, let txtNode = self.txtNode else
        {
            return ASLayoutSpec()
        }

        let stack = ASStackLayoutSpec()
        stack.direction = self.IsLayoutVertical ? .vertical : .horizontal
        stack.spacing = CGFloat(self.spacing)
        stack.style.height = ASDimension(unit: .points, value: CGFloat(height))
        stack.children = [svgNode, txtNode]
        stack.alignItems = .center
        stack.justifyContent = self.hAligment

        let inset = ASInsetLayoutSpec()
        inset.insets = UIEdgeInsets(top: 0,
                                    left: CGFloat(hPadding),
                                    bottom: 0,
                                    right: CGFloat(hPadding))
        inset.child = stack

        return inset
    }
}
