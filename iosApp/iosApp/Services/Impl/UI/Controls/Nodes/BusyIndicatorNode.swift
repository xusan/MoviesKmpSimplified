import AsyncDisplayKit
import UIKit

public class BusyIndicatorNode : ASDisplayNode
{
    private var activityIndicatorNode: ActivityIndicatorNode
    private var txtNode: ASTextNode
    private var rectangleNode: RectangleNode

    public var IsShowing: Bool = false

    // MARK: - Initializer

    public override init()
    {
        self.activityIndicatorNode = ActivityIndicatorNode(color: ColorConstants.PrimaryColor.ToUIColor())
        self.txtNode = ASTextNode()
        self.rectangleNode = RectangleNode(corner:16)

        super.init()

        self.automaticallyManagesSubnodes = true
        self.isHidden = true

        self.activityIndicatorNode.style.preferredSize = CGSize(width: 32, height: 32)
        self.SetText("On it...")

        self.rectangleNode.backgroundColor = ColorConstants.BgColor.ToUIColor()
    }

    // MARK: - Set Text

    public func SetText(_ text: String)
    {
        let attributes = [
            NSAttributedString.Key.font: UIFont(name: "Sen-SemiBold", size: 15)!,
            NSAttributedString.Key.foregroundColor: ColorConstants.Gray800.ToUIColor()
        ]

        self.txtNode.attributedText = NSAttributedString(string: text, attributes: attributes)
    }

    // MARK: - Layout

    public override func layoutSpecThatFits(_ constrainedSize: ASSizeRange) -> ASLayoutSpec
    {
        let stack = ASStackLayoutSpec()
        stack.direction = .vertical
        stack.justifyContent = .center
        stack.alignItems = .center
        stack.spacing = 7
        stack.children = [activityIndicatorNode, txtNode]

        let stackMargin = ASInsetLayoutSpec()
        stackMargin.insets = UIEdgeInsets(top: 20, left: 40, bottom: 20, right: 40)
        stackMargin.child = stack

        let bgRect = ASBackgroundLayoutSpec()
        bgRect.background = rectangleNode
        bgRect.child = stackMargin

        let centerContainer = ASCenterLayoutSpec()
        centerContainer.child = bgRect
        centerContainer.centeringOptions = .XY

        let overlay = ASDisplayNode()
        overlay.backgroundColor = UIColor.black.withAlphaComponent(0.6)

        let bg = ASBackgroundLayoutSpec()
        bg.background = overlay
        bg.child = centerContainer

        return bg
    }

    // MARK: - Show / Close

    public func Show()
    {
        guard !IsShowing else { return }

        self.IsShowing = true
        self.isHidden = false
        self.view.Fade(true)
    }

    public func Close()
    {
        self.view.Fade(false)
        {
            self.isHidden = true
        }
        self.IsShowing = false
    }
}
