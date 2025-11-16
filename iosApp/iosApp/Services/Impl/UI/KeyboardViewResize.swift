import SharedAppCore
import UIKit
import Foundation
import AsyncDisplayKit

///This class is helper for Text fields to make it visible when keyboard is showed.
///It has 2 mode page resize and pan
///It requires textfield to be inside scrollView
public class KeyboardViewResize
{
    private var bottomMargin: CGFloat
    private let page: iOSPage
    private var keyboardShowObserver: NSObjectProtocol?
    private var keyboardHideObserver: NSObjectProtocol?
    private var loggingService: ILoggingService?
    private var scrollNode: ASScrollNode!
    
    //If true then page will be resized otherwise page will be panned
    private var isResize: Bool = false

    init(page: iOSPage, scrollNode:ASScrollNode, resize: Bool)
    {
        do
        {
            self.page = page
            self.scrollNode = scrollNode
            self.isResize = resize
            self.bottomMargin = self.page.node.view.safeAreaInsets.bottom
            
            self.loggingService = try KoinResolver().GetLoggingService()
            //self.keyboardResizeType = try KoinResolver().GetKeyboardResizeType()
            self.RegisterForKeyboardNotifications()
        }
        catch
        {
            if loggingService != nil
            {
                loggingService?.LogWarning(message: error.localizedDescription)
            }
            else
            {
                print(error.localizedDescription)
            }
        }
    }

    public func GetBottomMargin() -> CGFloat
    {
        return self.bottomMargin
    }

    public func SetBottomMarin(value: CGFloat)
    {
        self.bottomMargin = value
    }

    public func RegisterForKeyboardNotifications()
    {
        if keyboardShowObserver == nil
        {
            keyboardShowObserver = NotificationCenter.default.addObserver(
                forName: UIResponder.keyboardWillShowNotification,
                object: nil,
                queue: .main)
            { [weak self] notification in
                self?.OnKeyboardShow(notification: notification)
            }
        }
        if keyboardHideObserver == nil
        {
            keyboardHideObserver = NotificationCenter.default.addObserver(
                forName: UIResponder.keyboardWillHideNotification,
                object: nil,
                queue: .main)
            { [weak self] notification in
                self?.OnKeyboardHide(notification: notification)
            }
        }
    }

    public func Destroy()
    {
        if let observer = keyboardShowObserver
        {
            NotificationCenter.default.removeObserver(observer)
            keyboardShowObserver = nil
        }

        if let observer = keyboardHideObserver
        {
            NotificationCenter.default.removeObserver(observer)
            keyboardHideObserver = nil
        }
    }

    private func OnKeyboardShow(notification: Notification)
    {
        if let userInfo = notification.userInfo,
           let keyboardFrame = userInfo[UIResponder.keyboardFrameEndUserInfoKey] as? NSValue
        {
            let keyboardSize = keyboardFrame.cgRectValue.size
            let keyboardHeight = CGFloat(keyboardSize.height)
            if self.isResize
            {
                self.bottomMargin = CGFloat(keyboardSize.height)
                self.page.node.setNeedsLayout()
                self.page.node.layoutIfNeeded()
            }
            else
            {
                scrollNode.view.contentInset.bottom = keyboardHeight + 20
                scrollNode.view.verticalScrollIndicatorInsets.bottom = keyboardHeight
                scrollNode.layoutIfNeeded()
                
                scrollToActiveField()
            }
        }
    }

    private func OnKeyboardHide(notification: Notification)
    {
        if isResize
        {
            self.bottomMargin = self.page.node.view.safeAreaInsets.bottom
            self.page.node.setNeedsLayout()
            self.page.node.layoutIfNeeded()
        }
        else
        {
            scrollNode.view.contentInset.bottom = 0
            scrollNode.view.verticalScrollIndicatorInsets.bottom = 0
        }
    }
    
    
    private func scrollToActiveField()
    {
        let txt = findFirstResponder()
        
        if let txt = txt
        {
            scrollNode.view.scrollRectToVisible(txt.frame , animated: true)
        }
    }
    
    func findFirstResponder() -> UIView?
    {
        return UIApplication.shared.windows
            .filter { $0.isKeyWindow }
            .first?
            .rootViewController?
            .view
            .findFirstResponder()
    }
}


