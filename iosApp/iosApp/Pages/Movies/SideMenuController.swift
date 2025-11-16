import Foundation
import AsyncDisplayKit
import SharedAppCore

class SideMenuController : iOSPage
{
    private var btnShareLogs: IconTextButtonNode!
    private var btnLogout: IconTextButtonNode!
    private var txtVersion: ASTextNode!
    private var spacer1: ASDisplayNode!
    private var spacer2: ASDisplayNode!
    
    override func InitializeNodes()
    {
        node.backgroundColor = .white
        
        btnShareLogs = GetMenuButton(icon: "logout.svg", text: "Share Logs")
        btnShareLogs.TouchUp.AddListener(listener_: btnShare_TouchUp)
        
        btnLogout = GetMenuButton(icon: "logout.svg", text: "Logout")
        btnLogout.TouchUp.AddListener(listener_: btnLogout_TouchUp)

        // Spacer 1
        spacer1 = ASDisplayNode()
        spacer1.style.preferredSize = CGSize(width: 60, height: 60)

        // Spacer 2
        spacer2 = ASDisplayNode()
        spacer2.style.flexGrow = 1

        // App version text
        let appVersion = "Version: \(Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "1.0") " +
                         "(\(Bundle.main.infoDictionary?["CFBundleVersion"] as? String ?? "1"))"

        let txtAttr: [NSAttributedString.Key: Any] = [
            .font: UIFont(name: "Sen-SemiBold", size: 12)!,
            .foregroundColor: ColorConstants.LabelColor.ToUIColor()
        ]

        txtVersion = ASTextNode()
        txtVersion.attributedText = NSAttributedString(string: appVersion, attributes: txtAttr)
    }
    
    override func LayoutSpecOverride(node: ASDisplayNode, constrainedSize: ASSizeRange) -> ASLayoutSpec
    {
        //Version label in bottom-right corner
        let relative = ASRelativeLayoutSpec(
            horizontalPosition: .end,
            verticalPosition: .end,
            sizingOption: [],
            child: txtVersion
        )
        
        let verInset = ASInsetLayoutSpec(
            insets: UIEdgeInsets(top: 30, left: 30, bottom: 50, right: 30),
            child: relative
        )
        
        // Vertical stack of all elements
        let stack = ASStackLayoutSpec.vertical()
        stack.spacing = 2
        stack.alignItems = .stretch
        stack.children = [spacer1, btnShareLogs, btnLogout, spacer2, verInset]
        
        return stack
    }
    
    func btnShare_TouchUp(btn: BaseControlNode?)
    {
        let homeVm = try! KoinResolver().GetNavigationService().GetCurrentPageModel() as? MoviesPageViewModel
        if let vm = homeVm
        {
            let menuItem = MenuItem()
            menuItem.Type = .sharelogs
            vm.MenuTappedCommand.Execute(param: menuItem)
            SceneDelegate.Instance.flyoutController.closeLeft()
        }
    }
    
    func btnLogout_TouchUp(btn: BaseControlNode?)
    {
        let homeVm = try! KoinResolver().GetNavigationService().GetCurrentPageModel() as? MoviesPageViewModel
        if let vm = homeVm
        {
            let menuItem = MenuItem()
            menuItem.Type = .logout
            vm.MenuTappedCommand.Execute(param: menuItem)
            SceneDelegate.Instance.flyoutController.closeLeft()
        }
    }
    
    private func GetMenuButton(icon: String, text: String) -> IconTextButtonNode
    {
        let button = IconTextButtonNode(normalColor: .white, pressedColor: ColorConstants.PrimaryColor2.ToUIColor())
        let font = UIFont(name: "Sen", size: 22)
        
        button.SetIconText(
            icon,
            text,
            spacing: 20,
            iconSize: 24,
            textFont: UIFont(name: "Sen", size: 22)!,
            hPadding: 30,
            hAligment: ASStackLayoutJustifyContent.start,
            corner: 0
        )
        
        return button
    }
}
