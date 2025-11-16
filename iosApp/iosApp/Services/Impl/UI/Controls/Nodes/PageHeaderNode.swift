import AsyncDisplayKit
import UIKit

public class PageHeaderNode : ASDisplayNode
{
    public var leftBtnNode: IconButtonNode?
    public var rightBtnNode: IconButtonNode?

    private var leftEmptySpacer: ASDisplayNode?
    private var rightEmptySpacer: ASDisplayNode?
    private var titleNode: ASTextNode

    // MARK: - Initializer

    public init(title: String, leftIcon: String? = nil, rightIcon: String? = nil)
    {
        self.titleNode = TextStyles.Create_pageMediumTitleStyle(title)
        super.init()

        self.automaticallyManagesSubnodes = true
        self.backgroundColor = ColorConstants.HeaderBgColor.ToUIColor()

        if let leftIcon = leftIcon
        {
            self.leftBtnNode = ButtonStyles.CreateIconButton(leftIcon)

            if rightIcon == nil, let leftBtnNode = self.leftBtnNode
            {
                let btnSize = leftBtnNode.svgNode.style.preferredSize.width + CGFloat(leftBtnNode.iconPadding)
                let rightSpacer = ASDisplayNode()
                rightSpacer.style.preferredSize = CGSize(width: btnSize, height: btnSize)
                self.rightEmptySpacer = rightSpacer
            }
        }

        if let rightIcon = rightIcon
        {
            self.rightBtnNode = ButtonStyles.CreateIconButton(rightIcon)

            if leftIcon == nil, let rightBtnNode = self.rightBtnNode
            {
                let btnSize = rightBtnNode.svgNode.style.preferredSize.width + CGFloat(rightBtnNode.iconPadding)
                let leftSpacer = ASDisplayNode()
                leftSpacer.style.preferredSize = CGSize(width: btnSize, height: btnSize)
                self.leftEmptySpacer = leftSpacer
            }
        }
    }

    // MARK: - Layout

    public override func layoutSpecThatFits(_ constrainedSize: ASSizeRange) -> ASLayoutSpec
    {
        let centerTitle = ASCenterLayoutSpec()
        centerTitle.child = titleNode
        centerTitle.centeringOptions = .XY
        centerTitle.style.flexGrow = 1

        let headerStack = ASStackLayoutSpec()
        headerStack.direction = .horizontal
        headerStack.spacing = 0
        headerStack.alignItems = .center

        if let leftBtn = leftBtnNode, let rightBtn = rightBtnNode
        {
            headerStack.children = [leftBtn, centerTitle, rightBtn]
        }
        else if let leftBtn = leftBtnNode, let rightSpacer = rightEmptySpacer
        {
            headerStack.children = [leftBtn, centerTitle, rightSpacer]
        }
        else if let leftSpacer = leftEmptySpacer, let rightBtn = rightBtnNode
        {
            headerStack.children = [leftSpacer, centerTitle, rightBtn]
        }
        else
        {
            headerStack.children = [centerTitle]
        }

        let headerInset = ASInsetLayoutSpec()
        headerInset.child = headerStack
        headerInset.insets = UIEdgeInsets(
            top: CGFloat(NumConstants.PageHeaderVPadding),
            left: CGFloat(NumConstants.PageHeaderHPadding),
            bottom: CGFloat(NumConstants.PageHeaderVPadding),
            right: CGFloat(NumConstants.PageHeaderHPadding)
        )

        return headerInset
    }
}
