import SharedAppCore
import UIKit
import Foundation
import LinkPresentation

class iOSShareImplementation: IShare
{
    func RequestShareFile(title: String, fullPath: String)
    {
        let fileUrl = URL(fileURLWithPath: fullPath)
        let shareItem = GetShareItem(fileUrl as NSObject, title: title)
        
        let activityController = UIActivityViewController(activityItems: [shareItem], applicationActivities: nil)
        
        // Get topmost UIViewController
        if let vc = CurrentController.GetTopViewController()
        {
            if let popover = activityController.popoverPresentationController
            {
                popover.sourceView = vc.view
                popover.sourceRect = vc.view?.bounds ?? .zero
            }
            
            vc.present(activityController, animated: true)
        }
    }
    
    func GetShareItem(_ obj: NSObject, title: String?) -> NSObject
    {
        if let title = title, !title.isEmpty
        {
            return ShareActivityItemSource(item: obj, title: title)
        }
        else
        {
            return obj
        }
    }
}

class ShareActivityItemSource: NSObject, UIActivityItemSource
{
    private let item: NSObject
    private let title: String
    
    init(item: NSObject, title: String)
    {
        self.item = item
        self.title = title
    }
    
    func activityViewControllerPlaceholderItem(_ activityViewController: UIActivityViewController) -> Any
    {
        return item
    }
    
    func activityViewController(_ activityViewController: UIActivityViewController, itemForActivityType activityType: UIActivity.ActivityType?) -> Any?
    {
        return item
    }
    
    func activityViewControllerLinkMetadata(_ activityViewController: UIActivityViewController) -> LPLinkMetadata?
    {
        let meta = LPLinkMetadata()
        
        if !title.isEmpty
        {
            meta.title = title
        }
        
        if let url = item as? NSURL
        {
            meta.url = url as URL
        }
        
        return meta
    }
}
