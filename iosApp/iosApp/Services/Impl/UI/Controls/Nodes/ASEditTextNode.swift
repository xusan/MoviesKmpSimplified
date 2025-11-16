import AsyncDisplayKit
import UIKit

public class ASEditTextNode : ASDisplayNode
{
    private var minAllowedHeight: Int = 45

    public var NormalBorderColor: UIColor = .clear
    public var FocusedBorderColor: UIColor = ColorConstants.PrimaryColor.ToUIColor()

    public var TextField: UITextField

    public override init()
    {
        self.TextField = UITextField()
        super.init()

        self.style.height = ASDimension(unit: .points, value: CGFloat(minAllowedHeight))

        self.TextField.backgroundColor = .white
        self.TextField.layer.cornerRadius = CGFloat(minAllowedHeight) / 2.0
        self.TextField.layer.borderWidth = 2
        self.TextField.layer.borderColor = NormalBorderColor.cgColor
        self.TextField.font = UIFont(name: "Sen", size: 15)
        self.TextField.textColor = .black

        self.TextField.leftView = UIView(frame: CGRect(x: 0, y: 0, width: 20, height: 10))
        self.TextField.leftViewMode = .always

        // set right search icon placeholder container
        let rightView = UIView(frame: CGRect(x: 0, y: 0, width: 20, height: 24))
        self.TextField.rightView = rightView
        self.TextField.rightViewMode = .always

        // Events (targets) to mirror C# handlers
        self.TextField.addTarget(self, action: #selector(TextField_EditingDidBegin(_:)), for: .editingDidBegin)
        self.TextField.addTarget(self, action: #selector(TextField_EditingDidEnd(_:)), for: .editingDidEnd)
        self.TextField.addTarget(self, action: #selector(TextField_EditingDidEndOnExit(_:)), for: .editingDidEndOnExit)
    }

    public override func didLoad()
    {
        super.didLoad()
        self.view.addSubview(self.TextField)
    }

    public override func layout()
    {
        super.layoutDidFinish()
        self.TextField.frame = CGRect(origin: .zero, size: self.calculatedSize)
    }

    @objc private func TextField_EditingDidBegin(_ sender: Any)
    {
        self.TextField.layer.borderColor = FocusedBorderColor.cgColor
    }

    @objc private func TextField_EditingDidEnd(_ sender: Any)
    {
        self.TextField.resignFirstResponder()
        self.TextField.layer.borderColor = NormalBorderColor.cgColor
    }

    @objc private func TextField_EditingDidEndOnExit(_ sender: Any)
    {
        self.TextField.resignFirstResponder()
        self.TextField.layer.borderColor = NormalBorderColor.cgColor
    }

    // Mirrors txtSearch_ShouldReturn behavior by handling "EditingDidEndOnExit"
    // If you truly need delegate-based return handling, you can wire UITextFieldDelegate instead.
    private func txtSearch_ShouldReturn(_ textField: UITextField) -> Bool
    {
        self.TextField.resignFirstResponder()
        self.TextField.layer.borderColor = NormalBorderColor.cgColor
        return false
    }
}
