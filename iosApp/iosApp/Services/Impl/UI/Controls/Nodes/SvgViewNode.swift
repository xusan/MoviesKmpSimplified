import AsyncDisplayKit
import UIKit
import SVGKit

public class SvgViewNode : ASDisplayNode
{
    private let svgFile: String
    private var svgImgView: SVGKFastImageView?

    // MARK: - Initializer

    public init(svgFile: String)
    {
        self.svgFile = svgFile
        super.init()
    }

    // MARK: - Lifecycle

    public override func didLoad()
    {
        super.didLoad()
        
        let paths = Bundle.main.paths(forResourcesOfType: "svg", inDirectory: nil)
        if let svgPath = paths.first(where: { URL(fileURLWithPath: $0).lastPathComponent == self.svgFile })
        {
            print("✅ Found SVG path:", svgPath)
            let svgResource = SVGKImage(contentsOfFile: svgPath)
            let svgView = SVGKFastImageView(svgkImage: svgResource)
            svgView?.isUserInteractionEnabled = false

            self.svgImgView = svgView
            self.view.isUserInteractionEnabled = false
            self.view.addSubview(svgView!)
        }
        else
        {
            print("❌ SVG file not found for name:", self.svgFile)
        }
    }

    public override func layoutDidFinish()
    {
        super.layoutDidFinish()

        if let svgView = self.svgImgView
        {
            svgView.frame = CGRect(origin: .zero, size: self.calculatedSize)
        }
    }
}
