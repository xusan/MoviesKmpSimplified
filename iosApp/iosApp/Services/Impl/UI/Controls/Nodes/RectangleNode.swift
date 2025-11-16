import SharedAppCore
import AsyncDisplayKit
import UIKit

public class RectangleNode : ASControlNode
{
    private let roundTop: Bool
    private let roundAll: Bool = false

    public var TouchUp = Event<RectangleNode>()
    public var TouchDown = Event<RectangleNode>()

    // MARK: - Initializer

    public init(corner: Int, roundTop: Bool = false)
    {
        self.roundTop = roundTop
        super.init()
        self.cornerRadius = CGFloat(corner)
    }

    // MARK: - Lifecycle

    public override func didLoad()
    {
        super.didLoad()
        self.clipsToBounds = true

        // Add touch targets
        self.addTarget(self, action: #selector(OnTouchDown(_:)), forControlEvents: .touchDown)
        self.addTarget(self, action: #selector(OnTouchUpInside(_:)), forControlEvents: .touchUpInside)
        self.addTarget(self, action: #selector(OnTouchCancel(_:)), forControlEvents: .touchUpOutside)
        self.addTarget(self, action: #selector(OnTouchCancel(_:)), forControlEvents: .touchCancel)

        if self.roundTop
        {
            self.view.layer.maskedCorners = [
                .layerMinXMinYCorner,
                .layerMaxXMinYCorner
            ]
        }
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
