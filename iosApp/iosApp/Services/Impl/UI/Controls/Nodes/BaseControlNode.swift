import SharedAppCore
import AsyncDisplayKit
import UIKit

public class BaseControlNode : ASControlNode
{
    public var NormalColor: UIColor
    public var PressedColor: UIColor
    public var SelectedColor: UIColor

    public var TouchUp = Event<BaseControlNode>()
    public var TouchDown = Event<BaseControlNode>()    

    // MARK: - Initializers

    public convenience init(normalColor: UIColor)
    {
        self.init(
            normalColor: normalColor,
            pressedColor: ColorConstants.Gray100.ToUIColor()
        )
    }

    public convenience init(normalColor: UIColor, pressedColor: UIColor)
    {
        self.init(
            normalColor: normalColor,
            pressedColor: pressedColor,
            selectedColor: pressedColor,
            isSelected: false
        )
    }

    public init(normalColor: UIColor, pressedColor: UIColor, selectedColor: UIColor, isSelected: Bool)
    {
        self.NormalColor = normalColor
        self.PressedColor = pressedColor
        self.SelectedColor = selectedColor

        super.init()

        self.automaticallyManagesSubnodes = true
        self.SetSelected(isSelected)
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

    // MARK: - Methods

    public func ChangeNormalColor(_ normalColor: UIColor)
    {
        self.NormalColor = normalColor
        self.backgroundColor = normalColor
    }

    public func SetSelected(_ isSelected: Bool)
    {
        self.isSelected = isSelected

        if isSelected
        {
            self.backgroundColor = self.SelectedColor
        }
        else
        {
            self.backgroundColor = self.NormalColor
        }
    }

    open func Initialize()
    {
        // Intended to be overridden by subclasses
    }

    // MARK: - Touch Events

    @objc open func OnTouchDown(_ sender: Any)
    {
        self.backgroundColor = self.PressedColor
        self.TouchDown.Invoke(value:self)
    }

    @objc open func OnTouchUpInside(_ sender: Any)
    {
        if self.isSelected
        {
            return
        }

        self.backgroundColor = self.NormalColor
        self.TouchUp.Invoke(value:self)
    }

    @objc open func OnTouchCancel(_ sender: Any)
    {
        self.backgroundColor = self.NormalColor
    }
}
