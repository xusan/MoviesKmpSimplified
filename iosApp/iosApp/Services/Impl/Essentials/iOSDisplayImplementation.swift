import SharedAppCore
import UIKit

class iOSDisplayImplementation: IDisplay
{
    func GetDisplayInfo() -> DisplayInfo
    {
        let screen = UIScreen.main
        let bounds = screen.bounds
        let scale = screen.scale
        let rate = Float(screen.maximumFramesPerSecond)
        
        // Width and height multiplied by scale (for pixel resolution)
        let width = Double(bounds.size.width * scale)
        let height = Double(bounds.size.height * scale)
        
        return DisplayInfo(
            Width:width,
            Height:height,
            Density:Double(scale),
            Orientation:CalculateOrientation(),
            Rotation:CalculateRotation(),
            RefreshRate:rate
        )
    }
    
    func GetDisplayKeepOnValue() -> Bool
    {
        return UIApplication.shared.isIdleTimerDisabled
    }
    
    func SetDisplayKeepOnValue(keepOn: Bool)
    {
        UIApplication.shared.isIdleTimerDisabled = keepOn
    }
    
    private func CalculateOrientation() -> DisplayOrientation
    {
        let orientation = UIApplication.shared.statusBarOrientation
        if orientation == .landscapeLeft || orientation == .landscapeRight
        {
            return DisplayOrientation.landscape
        }
        else
        {
            return DisplayOrientation.portrait
        }
    }
    
    private func CalculateRotation() -> DisplayRotation
    {
        switch UIApplication.shared.statusBarOrientation
        {
        case .portrait:
            return DisplayRotation.rotation0
        case .portraitUpsideDown:
            return DisplayRotation.rotation180
        case .landscapeLeft:
            return DisplayRotation.rotation270
        case .landscapeRight:
            return DisplayRotation.rotation90
        default:
            return DisplayRotation.unknown
        }
    }
}
