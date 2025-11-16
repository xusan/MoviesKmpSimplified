import UIKit
import AsyncDisplayKit

public final class TextStyles {
    
    public static func Get_pageTitle_Attr(_ text: String, centerText: Bool = true) -> NSAttributedString {
        let fullRange = NSRange(location: 0, length: text.count)
        let paragraphStyle = NSMutableParagraphStyle()
        paragraphStyle.alignment = .center
        
        let attrString = NSMutableAttributedString(string: text)
        attrString.addAttribute(.font, value: UIFont(name: "Sen-Bold", size: 24)!, range: fullRange)
        attrString.addAttribute(.foregroundColor, value: ColorConstants.DefaultTextColor.ToUIColor(), range: fullRange)
        
        if centerText {
            attrString.addAttribute(.paragraphStyle, value: paragraphStyle, range: fullRange)
        }
        return attrString
    }
    
    public static func Get_pageSubTitle_Attr(_ text: String, centerText: Bool = true) -> NSAttributedString {
        let fullRange = NSRange(location: 0, length: text.count)
        let paragraphStyle = NSMutableParagraphStyle()
        paragraphStyle.lineHeightMultiple = 1.5
        paragraphStyle.alignment = .center
        
        let attrString = NSMutableAttributedString(string: text)
        attrString.addAttribute(.font, value: UIFont(name: "Sen", size: 19)!, range: fullRange)
        attrString.addAttribute(.foregroundColor, value: ColorConstants.DefaultTextColor.ToUIColor(), range: fullRange)
        
        if centerText {
            attrString.addAttribute(.paragraphStyle, value: paragraphStyle, range: fullRange)
        }
        return attrString
    }
    
    public static func Get_pageMediumTitle_Attr() -> [NSAttributedString.Key: Any] {
        return [
            .font: UIFont(name: "Sen", size: 18)!,
            .foregroundColor: ColorConstants.DefaultTextColor.ToUIColor()
        ]
    }
    
    public static func Create_pageMediumTitleStyle(_ text: String) -> ASTextNode {
        let attr = Get_pageMediumTitle_Attr()
        let attrStr = NSAttributedString(string: text, attributes: attr)
        let txtNode = ASTextNode()
        txtNode.attributedText = attrStr
        txtNode.maximumNumberOfLines = 1
        txtNode.truncationMode = .byTruncatingTail
        txtNode.truncationAttributedText = NSAttributedString(string: "...", attributes: attr)
        return txtNode
    }
    
    public static func Get_pageTitleLoading_Attr() -> [NSAttributedString.Key: Any] {
        return [
            .font: UIFont(name: "Sen", size: 16)!,
            .foregroundColor: ColorConstants.DefaultTextColor.ToUIColor()
        ]
    }
    
    public static func Create_pageTitleLoadingStyle() -> ASTextNode {
        let attr = Get_pageTitleLoading_Attr()
        let attrStr = NSAttributedString(string: "Updating...", attributes: attr)
        let txtNode = ASTextNode()
        txtNode.attributedText = attrStr
        return txtNode
    }
    
    public static func Create_homepageMediumTitleStyle(_ text: String) -> NSAttributedString {
        let fullRange = NSRange(location: 0, length: text.count)
        let paragraphStyle = NSMutableParagraphStyle()
        paragraphStyle.lineSpacing = 4.0
        paragraphStyle.alignment = .center
        
        let attrString = NSMutableAttributedString(string: text)
        attrString.addAttribute(.font, value: UIFont(name: "Sen", size: 18)!, range: fullRange)
        attrString.addAttribute(.foregroundColor, value: ColorConstants.DefaultTextColor.ToUIColor(), range: fullRange)
        attrString.addAttribute(.paragraphStyle, value: paragraphStyle, range: fullRange)
        
        return attrString
    }
    
    public static func Create_centeredTextNode(_ text: String, fontSize: Int, color: UIColor, family: String = "Sen") -> ASTextNode {
        let fullRange = NSRange(location: 0, length: text.count)
        let paragraphStyle = NSMutableParagraphStyle()
        paragraphStyle.lineSpacing = 4.0
        paragraphStyle.alignment = .center
        
        let attrString = NSMutableAttributedString(string: text)
        attrString.addAttribute(.font, value: UIFont(name: family, size: CGFloat(fontSize))!, range: fullRange)
        attrString.addAttribute(.foregroundColor, value: color, range: fullRange)
        attrString.addAttribute(.paragraphStyle, value: paragraphStyle, range: fullRange)
        
        let txtNode = ASTextNode()
        txtNode.attributedText = attrString
        return txtNode
    }
    
    public static func Create_boldMediumStyle() -> [NSAttributedString.Key: Any] {
        return [
            .font: UIFont(name: "Sen-Bold", size: 16)!,
            .foregroundColor: ColorConstants.DefaultTextColor.ToUIColor()
        ]
    }
    
    public static func Create_regularMediumStyle() -> [NSAttributedString.Key: Any] {
        return [
            .font: UIFont(name: "Sen", size: 16)!,
            .foregroundColor: ColorConstants.DefaultTextColor.ToUIColor()
        ]
    }
    
    public static func Create_regularSmallStyle(_ text: String, textColor: UIColor, centerText: Bool = true) -> NSAttributedString {
        let fullRange = NSRange(location: 0, length: text.count)
        let paragraphStyle = NSMutableParagraphStyle()
        paragraphStyle.lineHeightMultiple = 1.3
        paragraphStyle.alignment = .center
        
        let attrString = NSMutableAttributedString(string: text)
        attrString.addAttribute(.font, value: UIFont(name: "Sen", size: 14)!, range: fullRange)
        attrString.addAttribute(.foregroundColor, value: textColor, range: fullRange)
        
        if centerText {
            attrString.addAttribute(.paragraphStyle, value: paragraphStyle, range: fullRange)
        }
        return attrString
    }
    
    public static func Create_FaStyle(_ color: UIColor, size: Int) -> [NSAttributedString.Key: Any] {
        return [
            .font: UIFont(name: "Font Awesome 5 Free Solid", size: CGFloat(size))!,
            .foregroundColor: color
        ]
    }
}
