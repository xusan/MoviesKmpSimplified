import Foundation
import SharedAppCore
import UIKit

class iOSDeviceInfoImplementation: IDeviceInfo {
    var Model: String {
        return UIDevice.current.model
    }

    var Manufacturer: String {
        return "Apple"
    }

    var Name: String {
        return UIDevice.current.name
    }

    var VersionString: String {
        return UIDevice.current.systemVersion
    }

    var Version: VersionInfo {
        return VersionInfo.companion.ParseVersion(version: VersionString)
    }

    var Platform: DevicePlatform {
        return DevicePlatform.companion.iOS
    }

    var Idiom: DeviceIdiom {
        switch UIDevice.current.userInterfaceIdiom {
        case .pad:
            return DeviceIdiom.companion.Tablet
        case .phone:
            return DeviceIdiom.companion.Phone
        case .tv:
            return DeviceIdiom.companion.TV
        case .carPlay, .unspecified:
            fallthrough
        default:
            return DeviceIdiom.companion.Unknown
        }
    }

    var DeviceType: DeviceTypeEnum {
        // Placeholder logic; replace if simulator detection is added later
        return DeviceTypeEnum.physical
    }
}
