import UIKit

class CurrentController
{
    public static func GetTopViewController(_ root: UIViewController? = nil) -> UIViewController?
    {
        let rootVC = root ?? UIApplication.shared.connectedScenes
        .compactMap{ $0 as? UIWindowScene }
        .flatMap{ $0.windows }
        .first{ $0.isKeyWindow }?.rootViewController

        if let nav = rootVC as? UINavigationController
        {
            return GetTopViewController(nav.visibleViewController)
        }
        else if let tab = rootVC as? UITabBarController
        {
            return GetTopViewController(tab.selectedViewController)
        }
        else if let presented = rootVC?.presentedViewController
        {
            return GetTopViewController(presented)
        }
        else
        {
            return rootVC
        }
    }
}
