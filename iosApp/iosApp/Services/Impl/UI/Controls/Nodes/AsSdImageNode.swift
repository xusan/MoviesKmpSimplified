import AsyncDisplayKit
import SDWebImage

public class AsSdImageNode: ASDisplayNode
{
    private let imageNode = ASImageNode()
    
    public var cornerRadiusValue: CGFloat = 0 {
        didSet { imageNode.cornerRadius = cornerRadiusValue }
    }
    public var url: String?
    public var placeholderName: String?
    public var filePath: String?
    
    public override init()
    {
        super.init()
        automaticallyManagesSubnodes = true
        imageNode.contentMode = .scaleAspectFit
        imageNode.clipsToBounds = true
    }

    public override func didLoad() {
        super.didLoad()
        Refresh()
    }

    public func Refresh()
    {
        if let path = filePath, FileManager.default.fileExists(atPath: path)
        {
            imageNode.image = UIImage(contentsOfFile: path)
        }
        else if let urlStr = url, let url = URL(string: urlStr)
        {
            imageNode.image = UIImage(named: placeholderName ?? "")
            SDWebImageManager.shared.loadImage(
                with: url,
                options: [],
                progress: nil
            ) { [weak self] image, _, _, _, _, _ in
                self?.imageNode.image = image
            }
        }
        else if let placeholder = placeholderName
        {
            imageNode.image = UIImage(named: placeholder)
        }
    }

    public override func layoutSpecThatFits(_ constrainedSize: ASSizeRange) -> ASLayoutSpec
    {
        ASWrapperLayoutSpec(layoutElement: imageNode)
    }

    public func Clear()
    {
        imageNode.image = nil
    }
}
