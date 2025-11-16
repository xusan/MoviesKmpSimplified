import AsyncDisplayKit
import Foundation
import SharedAppCore

class MovieDetailPage : iOSLifecyclePage
{
    private var headerNode: PageHeaderNode!
    private var imgNode: AsSdImageNode!
    private var lblName: ASTextNode!
    private var txtName: ASTextNode!
    private var lblDescription: ASTextNode!
    private var txtDescription: ASTextNode!
    
    var vm: MovieDetailPageViewModel
    {
      get { super.ViewModel as! MovieDetailPageViewModel }
      set { super.ViewModel = newValue }
    }
    
    override func InitializeNodes()
    {
        headerNode = PageHeaderNode(title: "Detail", leftIcon: "backarrowblack.svg", rightIcon: "edit.svg")
        headerNode.leftBtnNode?.TouchUp.AddListener(listener_: hederLeftBtnNode_TouchUp)
        headerNode.rightBtnNode?.TouchUp.AddListener(listener_: headerRightBtnNode_TouchUp)

        // MARK: - Image
        imgNode = AsSdImageNode()
        if let model = vm.Model, let imgPath = model.PosterUrl
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
        else
        {
            imgNode.Clear()
        }
        imgNode.style.preferredLayoutSize = ASLayoutSize(
            width: ASDimension(unit: .points, value: 200),
            height: ASDimension(unit: .points, value: 300)
        )

        // MARK: - Paragraph style for right-aligned labels
        let rightAligned = NSMutableParagraphStyle()
        rightAligned.alignment = .right

        // MARK: - Name Label
        lblName = ASTextNode()
        lblName.attributedText = NSAttributedString(
            string: "Name:",
            attributes: [
                .font: UIFont(name: "Sen", size: 15)!,
                .foregroundColor: ColorConstants.LabelColor.ToUIColor(),
                .paragraphStyle: rightAligned
            ]
        )

        // MARK: - Name Value
        txtName = ASTextNode()
        txtName.style.flexGrow = 1
        txtName.style.flexShrink = 1
        txtName.attributedText = NSAttributedString(
            string: vm.Model?.Name ?? "",
            attributes: [
                .font: UIFont(name: "Sen-SemiBold", size: 15)!,
                .foregroundColor: ColorConstants.LabelColor.ToUIColor()
            ]
        )

        // MARK: - Description Label
        lblDescription = ASTextNode()
        lblDescription.attributedText = NSAttributedString(
            string: "Description:",
            attributes: [
                .font: UIFont(name: "Sen", size: 15)!,
                .foregroundColor: ColorConstants.LabelColor.ToUIColor(),
                .paragraphStyle: rightAligned
            ]
        )

        // MARK: - Description Value
        txtDescription = ASTextNode()
        txtDescription.maximumNumberOfLines = 0
        txtDescription.truncationMode = .byWordWrapping
        txtDescription.style.flexGrow = 1
        txtDescription.style.flexShrink = 1
        txtDescription.attributedText = NSAttributedString(
            string: vm.Model?.Overview ?? "",
            attributes: [
                .font: UIFont(name: "Sen-Medium", size: 15)!,
                .foregroundColor: ColorConstants.LabelColor.ToUIColor()
            ]
        )
    }
    
    override func LayoutSpecOverride(node: ASDisplayNode, constrainedSize: ASSizeRange) -> ASLayoutSpec
    {
        // --- Measure widest label for alignment ---
        let maxLabelWidth = 90.0
        lblName.style.width = ASDimension(unit: .points, value: maxLabelWidth)
        lblDescription.style.width = ASDimension(unit: .points, value: maxLabelWidth)
       
       // --- Name Row ---
       let nameRow = ASStackLayoutSpec()
        nameRow.direction = .horizontal
        nameRow.spacing = 8
        nameRow.alignItems = .start
        nameRow.children = [lblName, txtName]
       
       // --- Description Row ---
       let descRow = ASStackLayoutSpec()
        descRow.direction = .horizontal
       descRow.spacing = 8
       descRow.alignItems = .start
       descRow.children = [lblDescription, txtDescription]
       
       // --- Rows Stack ---
       let rowsStack = ASStackLayoutSpec()
        rowsStack.direction = .vertical
       rowsStack.spacing = 8
       rowsStack.children = [nameRow, descRow]
       
       // --- Center image using CenterLayoutSpec ---
       let centeredImage = ASCenterLayoutSpec(
           centeringOptions: .X,
           sizingOptions: [],
           child: imgNode
       )
       
       // --- Content Stack ---
       let contentStack = ASStackLayoutSpec()
        contentStack.direction = .vertical
       contentStack.spacing = 25
       contentStack.alignItems = .stretch
       contentStack.children = [centeredImage, rowsStack]
       
//       // --- Insets ---
       let contentHorizontalInset = GetPageInsets()
       contentHorizontalInset.child = contentStack
       
       // --- Main Stack ---
       let mainStack = ASStackLayoutSpec()
        mainStack.direction = .vertical
       mainStack.spacing = 25
       mainStack.alignItems = .stretch
       mainStack.children = [headerNode, contentHorizontalInset]
       
       // --- Safe Area Inset ---
       let safeAreaInset = ASInsetLayoutSpec(
           insets: view.safeAreaInsets,
           child: mainStack
       )
       
       return safeAreaInset
    }
    
    override func OnViewModelPropertyChanged(propertyName: NSString?)
    {
        super.OnViewModelPropertyChanged(propertyName: propertyName)

        guard let propertyName = propertyName as String? else { return }
        
        if propertyName == AddEditMoviePageViewModel.companion.UPDATE_ITEM
        {
            self.InitializeNodes()//data is changes so we need to recreate views and apply data
            self.node.setNeedsLayout()
            self.node.layoutIfNeeded()
        }
    }
    
    func hederLeftBtnNode_TouchUp(node: BaseControlNode?)
    {
        self.OnBackBtnPressed()
    }
    
    func headerRightBtnNode_TouchUp(btn: BaseControlNode?)
    {
        vm.EditCommand.Execute()
    }
}
