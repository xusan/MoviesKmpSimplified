import SharedAppCore
import AsyncDisplayKit
import UIKit

public class SnackbarNode : ASDisplayNode
{
    private var btnNode: ButtonNode
    private var txtNode: ASTextNode
    private var rectangleNode: RectangleNode

    public var IsOpen: Bool = false

    // MARK: - Initializer

    public override init()
    {
        self.txtNode = ASTextNode()
        self.btnNode = ButtonNode(normalColor: .white, pressedColor: .white)
        self.rectangleNode = RectangleNode(corner: 16)

        super.init()

        self.automaticallyManagesSubnodes = true
        self.isHidden = true

        self.txtNode.maximumNumberOfLines = 5
        self.txtNode.style.flexGrow = 1
        self.txtNode.style.flexShrink = 1

        self.btnNode.style.preferredSize = CGSize(width: 80, height: 50)
        self.btnNode.style.flexShrink = 1
        self.btnNode.style.flexGrow = 0

        self.btnNode.TouchUp.AddListener(listener_: BtnNode_TouchUp)
        self.rectangleNode.TouchDown.AddListener(listener_: RectangleNode_TouchDown)
    }

    // MARK: - Configure

    public func SetText(_ text: String,
                        _ severity: SeverityType,
                        fontFamily: String? = nil,
                        fontFamily2: String? = nil)
    {
        
        self.rectangleNode.backgroundColor = SnackbarColors.shared.GetBackgroundColor(severity).ToUIColor()

        // --- Text ---
        var textAttr: [NSAttributedString.Key: Any] = [
            .foregroundColor: SnackbarColors.shared.GetTextColor(severity).ToUIColor()
        ]

        if let fontFamily = fontFamily
        {
            textAttr[.font] = UIFont(name: fontFamily, size: 15)
        }

        self.txtNode.attributedText = NSAttributedString(string: text, attributes: textAttr)

        // --- Button ---
        var btnAttr: [NSAttributedString.Key: Any] = [
            .foregroundColor: SnackbarColors.shared.GetTextColor(severity).ToUIColor()
        ]

        if let fontFamily2 = fontFamily2
        {
            btnAttr[.font] = UIFont(name: fontFamily2, size: 16)
        }

        self.btnNode.backgroundColor = SnackbarColors.shared.GetBackgroundColor(severity).ToUIColor()
        self.btnNode.normalColor = SnackbarColors.shared.GetBackgroundColor(severity).ToUIColor()
        self.btnNode.pressedColor = self.btnNode.normalColor.MakeDarker(0.1).withAlphaComponent(0.2)

        self.btnNode.setAttributedTitle(NSAttributedString(string: "Close", attributes: btnAttr), for: .normal)
    }

   

    // MARK: - Layout

    public override func layoutSpecThatFits(_ constrainedSize: ASSizeRange) -> ASLayoutSpec
    {
        // Horizontal stack: [Text | Button]
        let stack = ASStackLayoutSpec()
        stack.direction = .horizontal
        stack.justifyContent = .center
        stack.alignItems = .center
        stack.spacing = 7
        stack.children = [txtNode, btnNode]

        // Inner margin
        let stackMargin = ASInsetLayoutSpec()
        stackMargin.insets = UIEdgeInsets(top: 20, left: 20, bottom: 20, right: 20)
        stackMargin.child = stack

        // Background rectangle
        let bgRect = ASBackgroundLayoutSpec()
        bgRect.background = rectangleNode
        bgRect.child = stackMargin

        // Position it near bottom
        let rectInset = ASInsetLayoutSpec()
        rectInset.insets = UIEdgeInsets(top: 60, left: 25, bottom: CGFloat.infinity, right: 25)
        rectInset.child = bgRect

        // Semi-transparent background overlay
        let overlay = BackgroundNode(normalColor: UIColor.black.withAlphaComponent(0.6))
        overlay.TouchDown.AddListener(listener_: Overlay_TouchDown)

        let bg = ASBackgroundLayoutSpec()
        bg.background = overlay
        bg.child = rectInset

        return bg
    }
    
    func Overlay_TouchDown(btn: ASControlNode?)
    {
        self.Close()
    }
    
    func BtnNode_TouchUp(btn: ASButtonNode?)
    {
        self.Close()
    }
    
    func RectangleNode_TouchDown(btn: RectangleNode?)
    {
        self.Close()
    }
    
    // MARK: - Show / Close

    public func Show()
    {
        guard !IsOpen else { return }

        self.IsOpen = true
        self.isHidden = false
        self.view.Fade(true)
    }

    public func Close()
    {
        self.view.Fade(false)
        { [weak self] in
            self?.isHidden = true
        }
        self.IsOpen = false
    }
}
