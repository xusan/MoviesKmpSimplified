//
//  LoginPage.swift
//  iosApp
//
//  Created by xusan on 09/11/25.
//

import Foundation
import UIKit
import AsyncDisplayKit

class LoginPage: iOSLifecyclePage
{
    private var txtLogin: ASEditTextNode!
    private var txtPassword: ASEditTextNode!
    private var btnSubmit: ButtonNode!
    
    var vm: LoginPageViewModel
    {
      get { super.ViewModel as! LoginPageViewModel }
      set { super.ViewModel = newValue }
    }
    
    override func InitializeNodes()
    {
        txtLogin = ASEditTextNode()
        txtLogin.TextField.placeholder = "Login"
        
        // Password field
        txtPassword = ASEditTextNode()
        txtPassword.TextField.placeholder = "Password"
        txtPassword.TextField.isSecureTextEntry = true          // hide characters
        txtPassword.TextField.autocapitalizationType = .none
        txtPassword.TextField.autocorrectionType = .no
        txtPassword.TextField.clearButtonMode = .whileEditing
        txtPassword.TextField.returnKeyType = .done
        
        // Submit button
        btnSubmit = ButtonStyles.CreatePrimaryButton("Submit")
        
        txtLogin.TextField.addTarget(self, action: #selector(txtLogin_EditingChanged), for: .editingChanged)
        txtPassword.TextField.addTarget(self, action: #selector(txtPassword_EditingChanged), for: .editingChanged)
        self.btnSubmit.TouchUp.AddListener(listener_: btnSubmit_TouchUp)
    }
    
    override func LayoutSpecOverride(node: ASDisplayNode, constrainedSize: ASSizeRange) -> ASLayoutSpec
    {        
        // Vertical stack
        let verticalStack = ASStackLayoutSpec()
        verticalStack.direction = .vertical
        verticalStack.spacing = 16
        verticalStack.justifyContent = .center
        verticalStack.alignItems = .stretch
        verticalStack.children = [txtLogin, txtPassword, btnSubmit]
        
        // Add insets (margins)
        let insetSpec = self.GetPageInsets()
        insetSpec.child = verticalStack
        
        // Center everything vertically
        let centerSpec = ASCenterLayoutSpec()
        centerSpec.centeringOptions = .Y
        centerSpec.sizingOptions = .minimumXY
        centerSpec.child = insetSpec
        
        return centerSpec
    }
    
    @objc func txtLogin_EditingChanged(_ sender: Any)
    {
        vm.Login = txtLogin.TextField.text ?? ""
    }
    
    @objc func txtPassword_EditingChanged(_ sender: Any)
    {
        vm.Password = txtPassword.TextField.text ?? ""
    }
    
    func btnSubmit_TouchUp(btn: ASButtonNode?)
    {
        vm.SubmitCommand.Execute()
    }
}
