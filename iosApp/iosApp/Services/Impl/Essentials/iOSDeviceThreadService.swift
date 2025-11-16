import Foundation
import SharedAppCore

class iOSDeviceThreadService: IDeviceThreadService {
    func BeginInvokeOnMainThread(action: @escaping () -> Void) {
        DispatchQueue.main.async {
            action()
        }
    }
}
