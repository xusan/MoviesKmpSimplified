import AsyncDisplayKit
import Foundation
import SharedAppCore

class AddEditMoviePage : iOSLifecyclePage
{
    private var headerNode: PageHeaderNode!
    private var btnPhoto: BackgroundNode!
    private var imgNode: AsSdImageNode!
    private var photoIcon: SvgViewNode!
    private var lblName: ASTextNode!
    private var lblDescription: ASTextNode!
    private var txtName: ASEditTextNode!
    private var txtDescription: ASEditTextMultilineNode!
    private var btnSave: ButtonNode!
    private let scrollNode = ASScrollNode()
    private var keyboardViewResize: KeyboardViewResize!
    
    var vm: AddEditMoviePageViewModel
    {
      get { super.ViewModel as! AddEditMoviePageViewModel }
      set { super.ViewModel = newValue }
    }
    
    override func InitializeNodes()
    {
        self.headerNode = PageHeaderNode(title: vm.Title, leftIcon: "backarrowblack.svg",rightIcon: "deleteblack.svg"
        )
        
        headerNode.leftBtnNode?.TouchUp.AddListener(listener_: hederLeftBtnNode_TouchUp)
        headerNode.rightBtnNode?.TouchUp.AddListener(listener_: headerRightBtnNode_TouchUp)

        // --- Photo Button ---
        btnPhoto = BackgroundNode(
            normalColor: UIColor(red: 1, green: 1, blue: 1, alpha: 0.5),
            pressedColor: ColorConstants.Gray100.ToUIColor().withAlphaComponent(0.4)
        )
        btnPhoto.TouchUp.AddListener(listener_: btnPhoto_TouchUp)

        // --- Photo Icon ---
        photoIcon = SvgViewNode(svgFile: "icon_photo.svg")
        photoIcon.isUserInteractionEnabled = false
        photoIcon.style.preferredLayoutSize = ASLayoutSize(
            width: ASDimension(unit: .points, value: 45),
            height: ASDimension(unit: .points, value: 45)
        )

        // --- Image ---
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
        imgNode.style.preferredLayoutSize = ASLayoutSize(
            width: ASDimension(unit: .points, value: 200),
            height: ASDimension(unit: .points, value: 300)
        )

        // Right-aligned paragraph
        let rightAligned = NSMutableParagraphStyle()
        rightAligned.alignment = .right

        // --- Name Label ---
        lblName = ASTextNode()
        lblName.attributedText = NSAttributedString(
            string: "Name:",
            attributes: [
                .font: UIFont(name: "Sen", size: 15)!,
                .foregroundColor: ColorConstants.LabelColor.ToUIColor(),
                .paragraphStyle: rightAligned
            ]
        )

        // --- Name Value ---
        txtName = ASEditTextNode()
        txtName.TextField.text = vm.Model?.Name
        txtName.style.flexGrow = 1
        txtName.style.flexShrink = 1
        txtName.TextField.addTarget(self, action: #selector(txtName_EditingChanged), for: .editingChanged)

        // --- Description Label ---
        lblDescription = ASTextNode()
        lblDescription.attributedText = NSAttributedString(
            string: "Description:",
            attributes: [
                .font: UIFont(name: "Sen", size: 15)!,
                .foregroundColor: ColorConstants.LabelColor.ToUIColor(),
                .paragraphStyle: rightAligned
            ]
        )

        // --- Description Value ---
        txtDescription = ASEditTextMultilineNode()
        txtDescription.textView.text = vm.Model?.Overview
        txtDescription.style.flexGrow = 1
        //txtDescription.style.maxHeight = ASDimension(unit: .points, value: 200)
        txtDescription.TextChanged.AddListener(listener_: txtDescription_EditingChanged)

        // --- Save Button ---
        btnSave = ButtonStyles.CreatePrimaryButton("Save")
        btnSave.TouchUp.AddListener(listener_: btnSave_TouchUp)
                
        scrollNode.automaticallyManagesSubnodes = true
        scrollNode.automaticallyManagesContentSize = true
        scrollNode.view.alwaysBounceVertical = true
        scrollNode.layoutSpecBlock = { [weak self] _, constrainedSize in
            guard let self = self else { return ASLayoutSpec() }
            return self.scrollContentLayoutSpec(constrainedSize: constrainedSize)
        }
        
        keyboardViewResize = KeyboardViewResize(page: self, scrollNode: scrollNode, resize: false)
    }
    
    //Get layout that inside scroll view
    func scrollContentLayoutSpec(constrainedSize: ASSizeRange) -> ASLayoutSpec
    {
        // --- Clickable image overlay ---
        let clickableImage = ASOverlayLayoutSpec(
            child: imgNode,
            overlay: btnPhoto
        )

        let centerIcon = ASCenterLayoutSpec(
            centeringOptions: .XY,
            sizingOptions: .minimumXY,
            child: photoIcon
        )

        let imgOverlay = ASOverlayLayoutSpec(child: clickableImage, overlay: centerIcon)

        lblName.style.width = .init(unit: .points, value: 90)
        lblDescription.style.width = .init(unit: .points, value: 90)

        let nameRow = ASStackLayoutSpec.horizontal()
        nameRow.spacing = 8
        nameRow.alignItems = .center
        nameRow.children = [lblName, txtName]

        let descRow = ASStackLayoutSpec.horizontal()
        descRow.spacing = 8
        descRow.alignItems = .center
        descRow.children = [lblDescription, txtDescription]

        let rowsStack = ASStackLayoutSpec.vertical()
        rowsStack.spacing = 8
        rowsStack.children = [nameRow, descRow]

        let centeredImage = ASCenterLayoutSpec(
            centeringOptions: .X,
            sizingOptions: [],
            child: imgOverlay
        )

        let spacer = ASDisplayNode()
        spacer.style.flexGrow = 1

        let contentStack = ASStackLayoutSpec.vertical()
        contentStack.spacing = 25
        contentStack.alignItems = .stretch
        contentStack.children = [centeredImage, rowsStack, spacer,  btnSave]

        let inset = GetPageInsets()
        inset.child = contentStack

        return inset
    }

    //Get layout for whole page
    override func LayoutSpecOverride(node: ASDisplayNode, constrainedSize: ASSizeRange) -> ASLayoutSpec
    {
        scrollNode.style.flexShrink = 1   // allow shrinking instead of growing
        scrollNode.style.flexGrow = 0
        
        let mainStack = ASStackLayoutSpec.vertical()
        mainStack.spacing = 25
        mainStack.alignItems = .stretch
        mainStack.children = [headerNode, scrollNode]
        
        return ASInsetLayoutSpec(
            insets: self.view.safeAreaInsets,
            child: mainStack
        )
    }
    
    override func OnViewModelPropertyChanged(propertyName: NSString?)
    {
        super.OnViewModelPropertyChanged(propertyName: propertyName)

        guard let propertyName = propertyName as String? else { return }
        
        if propertyName == AddEditMoviePageViewModel.companion.PhotoChangedEvent
        {
            OnPhotoChanged()
        }
    }
    
    override func Destroy()
    {
        super.Destroy()
        keyboardViewResize.Destroy()
    }
    
    func hederLeftBtnNode_TouchUp(node: BaseControlNode?)
    {
        self.OnBackBtnPressed()
    }
    
    func headerRightBtnNode_TouchUp(btn: BaseControlNode?)
    {
        vm.DeleteCommand.Execute()
    }
    
    func btnPhoto_TouchUp(btn: BaseControlNode?)
    {
        vm.ChangePhotoCommand.Execute()
    }
    
    @objc func txtName_EditingChanged(_ sender: Any)
    {
        vm.Model?.Name = txtName.TextField.text ?? ""
    }
    
    @objc func txtDescription_EditingChanged(sender: ASEditTextMultilineNode?)
    {
        vm.Model?.Overview = txtDescription.textView.text ?? ""
    }
    
    func btnSave_TouchUp(btn: ASButtonNode?)
    {
        vm.SaveCommand.Execute()
    }
    
    func OnPhotoChanged()
    {
        if let url = vm.Model?.PosterUrl
        {
            imgNode.filePath = url
            imgNode.Refresh()
        }
        else
        {
            imgNode.Clear()
        }
    }
}
