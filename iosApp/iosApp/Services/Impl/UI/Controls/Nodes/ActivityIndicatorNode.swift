import AsyncDisplayKit
import UIKit

public class ActivityIndicatorNode : ASDisplayNode
{
    private var color: UIColor
    private var activityIndicatorView: UIActivityIndicatorView!

    public init(color: UIColor)
    {
        self.color = color
        super.init()
    }

    public override func didLoad()
    {
        super.didLoad()

        self.activityIndicatorView = UIActivityIndicatorView(style: .white)
        self.activityIndicatorView.color = color
        self.view.addSubview(self.activityIndicatorView)

        self.activityIndicatorView.startAnimating()
    }

    public override func layout()
    {
        super.layoutDidFinish()

        self.activityIndicatorView.frame = CGRect(origin: CGPoint.zero, size: self.calculatedSize)
    }
}
