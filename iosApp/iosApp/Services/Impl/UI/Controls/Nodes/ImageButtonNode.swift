import SharedAppCore
import AsyncDisplayKit
import UIKit
import SDWebImage

public class ImageButtonNode : ASControlNode
{
    public var Url: String
    public var CornerR: Int

    public var TouchUp = Event<ImageButtonNode>()
    public var TouchDown = Event<ImageButtonNode>()

    private var imgView: UIImageView!

    // MARK: - Initializer

    public init(url: String, cornerRadius: Int)
    {
        self.Url = url
        self.CornerR = cornerRadius
        super.init()
    }

    // MARK: - Lifecycle

    public override func didLoad()
    {
        super.didLoad()

        // Add touch event handlers
        self.addTarget(self, action: #selector(OnTouchDown(_:)), forControlEvents: .touchDown)
        self.addTarget(self, action: #selector(OnTouchUpInside(_:)), forControlEvents: .touchUpInside)
        self.addTarget(self, action: #selector(OnTouchCancel(_:)), forControlEvents: .touchUpOutside)
        self.addTarget(self, action: #selector(OnTouchCancel(_:)), forControlEvents: .touchCancel)

        // Configure image view
        self.imgView = UIImageView()
        self.imgView.contentMode = .scaleAspectFill
        self.imgView.layer.cornerRadius = CGFloat(CornerR)
        self.imgView.layer.masksToBounds = true
        self.imgView.layer.shouldRasterize = false

        // Load image via SDWebImage
        if let url = URL(string: Url)
        {
            self.imgView.sd_setImage(with: url)
        }

        self.view.addSubview(imgView)
    }

    public override func layout()
    {
        super.layoutDidFinish()
        self.imgView.frame = CGRect(origin: .zero, size: self.calculatedSize)
    }

    // MARK: - Touch Handlers

    @objc public func OnTouchDown(_ sender: Any)
    {
        self.TouchDown.Invoke(value: self)
    }

    @objc public func OnTouchUpInside(_ sender: Any)
    {
        self.TouchUp.Invoke(value: self)
    }

    @objc public func OnTouchCancel(_ sender: Any)
    {
        // Optional cancel handling
    }
}
