import SharedAppCore
import Foundation
import AsyncDisplayKit
import UIKit


final class CellMovie : ASCellNode
{
    private let backgroundNode: BackgroundNode
    private let imgNode: AsSdImageNode
    private let txtName: ASTextNode
    private let txtDescription: ASTextNode
    private let model: MovieItemViewModel

    init(model: MovieItemViewModel)
    {
        self.model = model
        self.backgroundNode = BackgroundNode(normalColor: .white)
        self.imgNode = AsSdImageNode()        
        self.txtName = ASTextNode()
        self.txtDescription = ASTextNode()

        super.init()

        // Automatically manage subnodes
        automaticallyManagesSubnodes = true

        backgroundNode.TouchUp.AddListener(listener_: onBackgroundTapped)

        if let imgPath = model.PosterUrl
        {
            if imgPath.isLocalFilePath()
            {
                imgNode.filePath = imgPath
            }
            else
            {
                imgNode.url = imgPath
            }
        }
        imgNode.style.preferredLayoutSize = ASLayoutSize(
            width: ASDimension(unit: .points, value: 100),
            height: ASDimension(unit: .points, value: 120)
        )

        // Name Text Node
        let nameAttributes: [NSAttributedString.Key: Any] =
        [
            .font: UIFont(name: "Sen-Bold", size: 14)!,
            .foregroundColor: ColorConstants.LabelColor.ToUIColor()
        ]

        txtName.attributedText = NSAttributedString(string: model.Name, attributes: nameAttributes)

        // Description Text Node
        let descAttributes: [NSAttributedString.Key: Any] =
        [
            .font: UIFont(name: "Sen-Regular", size: 14)!,
            .foregroundColor: ColorConstants.LabelColor.ToUIColor()
        ]

        txtDescription.attributedText = NSAttributedString(string: model.Overview, attributes: descAttributes)
    }

    override func layoutSpecThatFits(_ constrainedSize: ASSizeRange) -> ASLayoutSpec
    {
        // Vertical stack for name and description
        let textStack = ASStackLayoutSpec()
        textStack.direction = .vertical
        textStack.spacing = 5
        textStack.children = [txtName, txtDescription]
        textStack.style.flexShrink = 1

        // Horizontal stack: image + text
        let horizontalStack = ASStackLayoutSpec()
        horizontalStack.direction = .horizontal
        horizontalStack.spacing = 5
        horizontalStack.children = [imgNode, textStack]

        let insetSpec = ASInsetLayoutSpec(
            insets: UIEdgeInsets(top: 15, left: 15, bottom: 15, right: 20),
            child: horizontalStack
        )

        let overlay = ASBackgroundLayoutSpec()
        overlay.background = backgroundNode
        overlay.child = insetSpec

        return overlay
    }

    private func onBackgroundTapped(node: BaseControlNode?)
    {
        let pageNavigationService = try! KoinResolver().GetNavigationService()
        if let vm = pageNavigationService.GetCurrentPageModel() as? MoviesPageViewModel
        {
            vm.ItemTappedCommand.Execute(param: model)
        }
    }
}
