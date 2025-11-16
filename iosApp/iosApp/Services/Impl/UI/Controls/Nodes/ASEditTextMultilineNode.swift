import AsyncDisplayKit
import UIKit

class ASEditTextMultilineNode: ASEditableTextNode, UITextViewDelegate {
    
    // MARK: - Configurable properties
    var focusedBorderColor = ColorConstants.PrimaryColor.ToUIColor()
    var unfocusedBorderColor: UIColor = UIColor.clear
    var BorderWidth: CGFloat = 2
    var CornerRadius: CGFloat = 25       // half of 150 height, like your C# code
    var fixedHeight: CGFloat = 150
    var TextChanged = Event<ASEditTextMultilineNode>()
    
    // MARK: - Initialization
    override init()
    {
        super.init()
        
        // UITextView delegate for focus/unfocus
        self.textView.delegate = self
        
        // MULTILINE behavior (scroll inside, not auto-grow)
        self.textView.isScrollEnabled = true
        self.maximumLinesToDisplay = 0
        
        // UI appearance        
        self.textView.layer.borderWidth = BorderWidth
        self.textView.layer.cornerRadius = CornerRadius
        self.textView.layer.borderColor = unfocusedBorderColor.cgColor
        self.textView.layer.backgroundColor = UIColor.white.cgColor
        
        // Padding
        self.textContainerInset = UIEdgeInsets(top: 12, left: 15, bottom: 10, right: 50)
        
        // Also apply to initial text
        self.textView.font = UIFont(name: "Sen", size: 15)
        self.textView.textColor = UIColor.black
        
        // FIXED height
        self.style.height = ASDimension(unit: .points, value: fixedHeight)
        
        // Allow horizontal flex if needed
        self.style.flexGrow = 1
        self.style.flexShrink = 1
    }
    
    required override init(textKitComponents: ASTextKitComponents, placeholderTextKitComponents: ASTextKitComponents)
    {
        super.init(textKitComponents: textKitComponents,
                   placeholderTextKitComponents: placeholderTextKitComponents)
    }
    
    func textViewDidChange(_ textView: UITextView)
    {
        self.TextChanged.Invoke(value:self)
    }
    
    func textViewDidBeginEditing(_ textView: UITextView)
    {
        self.textView.layer.borderColor = focusedBorderColor.cgColor
    }
    
    func textViewDidEndEditing(_ textView: UITextView)
    {
        self.textView.layer.borderColor = unfocusedBorderColor.cgColor
    }
}
