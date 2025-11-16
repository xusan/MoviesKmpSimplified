import SharedAppCore
import AsyncDisplayKit
import UIKit

public class ButtonNode : ASButtonNode
{
    public var normalColor: UIColor
    public var pressedColor: UIColor

    public var TouchUp = Event<ASButtonNode>()
    public var TouchDown = Event<ASButtonNode>()

    // MARK: - Initializer

    public init(normalColor: UIColor, pressedColor: UIColor)
    {
        self.normalColor = normalColor
        self.pressedColor = pressedColor

        super.init()

        self.backgroundColor = normalColor
    }

    // MARK: - Lifecycle

    public override func didLoad()
    {
        super.didLoad()

        self.addTarget(self, action: #selector(OnTouchDown(_:)), forControlEvents: .touchDown)
        self.addTarget(self, action: #selector(OnTouchUpInside(_:)), forControlEvents: .touchUpInside)
        self.addTarget(self, action: #selector(OnTouchCancel(_:)), forControlEvents: .touchUpOutside)
        self.addTarget(self, action: #selector(OnTouchCancel(_:)), forControlEvents: .touchCancel)
    }

    // MARK: - Touch Handlers

    @objc public func OnTouchDown(_ sender: Any)
    {
        self.backgroundColor = self.pressedColor
        self.TouchDown.Invoke(value:self)
    }

    @objc public func OnTouchUpInside(_ sender: Any)
    {
        self.backgroundColor = self.normalColor
        self.TouchUp.Invoke(value:self)
    }

    @objc public func OnTouchCancel(_ sender: Any)
    {
        self.backgroundColor = self.normalColor
    }
}
