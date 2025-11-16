import SharedAppCore
import UIKit
import Foundation

@MainActor
final class iOSAlertDialogService: IAlertDialogService
{
    func DisplayAlert(title: String, message: String, cancel: String) async throws
    {
        _ = try await presentAlert(title: title, message: message, accept: nil, cancel: cancel)
    }
    
    func ConfirmAlert(title: String, message: String, buttons: KotlinArray<NSString>) async throws -> KotlinBoolean
    {
        let swiftButtons = (0..<buttons.size).compactMap { buttons.get(index: $0) as String? }
        let accept = swiftButtons.first
        let cancel = swiftButtons.dropFirst().first
        let result = try await presentAlert(title: title, message: message, accept: accept, cancel: cancel)
        return KotlinBoolean(bool: result)
    }
    
    func DisplayActionSheet(title: String, buttons: KotlinArray<NSString>) async throws -> String?
    {
        let swiftButtons = (0..<buttons.size).compactMap { buttons.get(index: $0) as String? }
        return try await presentActionSheet(title: title, cancel: nil, destruction: nil, buttons: swiftButtons)
    }
    
    func DisplayActionSheet(title: String,
                            cancel: String?,
                            destruction: String?,
                            buttons: KotlinArray<NSString>) async throws -> String?
    {
        let swiftButtons = (0..<buttons.size).compactMap { buttons.get(index: $0) as String? }
        return try await presentActionSheet(title: title, cancel: cancel, destruction: destruction, buttons: swiftButtons)
    }
    
    // MARK: - Private Helpers
    
    private func presentAlert(title: String?,
                              message: String?,
                              accept: String?,
                              cancel: String?) async throws -> Bool
    {
        try await withCheckedThrowingContinuation { continuation in
            guard let vc = CurrentController.GetTopViewController()
            else
            {
                continuation.resume(throwing: NSError(domain: "AlertError", code: 1, userInfo: nil))
                return
            }
            
            let alert = UIAlertController(title: title, message: message, preferredStyle: .alert)
            
            if let cancel = cancel
            {
                alert.addAction(UIAlertAction(title: cancel, style: .cancel)
                {
                    _ in continuation.resume(returning: false)
                })
            }
            
            if let accept = accept
            {
                alert.addAction(UIAlertAction(title: accept, style: .default)
                {
                    _ in continuation.resume(returning: true)
                })
            }
            else
            {
                alert.addAction(UIAlertAction(title: "OK", style: .default)
                {
                    _ in continuation.resume(returning: true)
                })
            }
            
            vc.present(alert, animated: true)
        }
    }
    
    private func presentActionSheet(title: String?,
                                    cancel: String?,
                                    destruction: String?,
                                    buttons: [String]) async throws -> String?
    {
        try await withCheckedThrowingContinuation { continuation in
            guard let vc = CurrentController.GetTopViewController()
            else
            {
                continuation.resume(throwing: NSError(domain: "ActionSheetError", code: 1, userInfo: nil))
                return
            }
            
            let sheet = UIAlertController(title: title, message: nil, preferredStyle: .actionSheet)
            
            // Regular buttons
            for button in buttons
            {
                sheet.addAction(UIAlertAction(title: button, style: .default)
                {
                    _ in continuation.resume(returning: button)
                })
            }
            
            // Destructive option
            if let destruction = destruction
            {
                sheet.addAction(UIAlertAction(title: destruction, style: .destructive)
                {
                    _ in continuation.resume(returning: destruction)
                })
            }
            
            // Cancel option
            if let cancel = cancel
            {
                sheet.addAction(UIAlertAction(title: cancel, style: .cancel)
                {
                    _ in continuation.resume(returning: cancel)
                })
            }
            
            // iPad popover positioning
            if let popover = sheet.popoverPresentationController
            {
                popover.sourceView = vc.view
                popover.sourceRect = vc.view.bounds
                popover.permittedArrowDirections = []
            }
            
            vc.present(sheet, animated: true)
        }
    }
}
