
import Foundation
import SharedAppCore
import UIKit

class IOSAppInfoImplementation: IAppInfo {
    var PackageName: String {
        return GetBundleValue(Key: "CFBundleIdentifier") ?? ""
    }

    var Name: String {
        return GetBundleValue(Key: "CFBundleDisplayName")
            ?? GetBundleValue(Key: "CFBundleName")
            ?? ""
    }

    var Version: VersionInfo {
        return SharedAppCore.VersionInfo.Companion.shared.ParseVersion(version: VersionString)
    }

    var VersionString: String {
        return GetBundleValue(Key: "CFBundleShortVersionString") ?? ""
    }

    var BuildString: String {
        return GetBundleValue(Key: "CFBundleVersion") ?? ""
    }

    func GetBundleValue(Key: String) -> String? {
        guard let dict = Bundle.main.infoDictionary,
              let value = dict[Key]
        else {
            return nil
        }
        return "\(value)"
    }

    func ShowSettingsUI() {
        if let settingsUrl = URL(string: UIApplication.openSettingsURLString),
           UIApplication.shared.canOpenURL(settingsUrl)
        {
            UIApplication.shared.open(settingsUrl, options: [:], completionHandler: nil)
        }
    }

    var RequestedLayoutDirection: LayoutDirection {
        let dir: UIUserInterfaceLayoutDirection

        if let currentWindow = UIApplication.shared.windows.first {
            dir = currentWindow.effectiveUserInterfaceLayoutDirection
        } else {
            dir = UIApplication.shared.userInterfaceLayoutDirection
        }

        return dir == .rightToLeft ? .righttoleft : .lefttoright
    }
}
