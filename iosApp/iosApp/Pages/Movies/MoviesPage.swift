import Foundation
import AsyncDisplayKit
import UIKit
import SharedAppCore

final class MoviesPage : iOSLifecyclePage
{
    @objc var vm: MoviesPageViewModel
    {
      get { super.ViewModel as! MoviesPageViewModel }
      set { super.ViewModel = newValue }
    }
    
    private var headerNode: PageHeaderNode!
    private var refreshControl: UIRefreshControl!
    internal var tableNode: ASTableNode!
    private var moviesDataSource: MoviesDataSource!

    override func InitializeNodes()
    {
        self.headerNode = PageHeaderNode(
            title: "Movies",
            leftIcon: "threeline.svg",
            rightIcon: "plus.svg"
        )

        self.tableNode = ASTableNode()
        self.moviesDataSource = MoviesDataSource(pageNode: self)
        self.refreshControl = UIRefreshControl()

        // Header button actions
        self.headerNode.leftBtnNode?.TouchUp.AddListener(listener_: onLeftButtonTapped)
        self.headerNode.rightBtnNode?.TouchUp.AddListener(listener_: onRightButtonTapped)

        // Table node setup
        self.tableNode.style.flexGrow = 1.0
        self.tableNode.dataSource = moviesDataSource
        //self.tableNode.delegate = MoviesDelegate()
        self.tableNode.view.separatorStyle = .singleLine

        // Refresh control setup
        self.refreshControl.addTarget(self, action: #selector(onRefresh), for: .valueChanged)
        self.tableNode.view.refreshControl = self.refreshControl
    }
    
    override func LayoutSpecOverride(node: ASDisplayNode, constrainedSize: ASSizeRange) -> ASLayoutSpec
    {        
        let verticalStack = ASStackLayoutSpec()
        verticalStack.direction = .vertical
        verticalStack.alignItems = .stretch
        verticalStack.justifyContent = .start
        verticalStack.children = [headerNode, tableNode]

        let inset = ASInsetLayoutSpec(insets: view.safeAreaInsets, child: verticalStack)
        return inset
    }
    
    override func OnViewModelPropertyChanged(propertyName: NSString?)
    {
        super.OnViewModelPropertyChanged(propertyName: propertyName)

        guard let propertyName = propertyName as String? else { return }
        
        if propertyName == #keyPath(vm.MovieItems).propertyName()
        {            
            moviesDataSource.onCollectionSet()
        }
        else if propertyName == #keyPath(vm.IsRefreshing).propertyName()
        {
            if vm.IsRefreshing == false
            {
                refreshControl.endRefreshing()
            }
        }
    }

    private func onRightButtonTapped(node: BaseControlNode?)
    {
        self.vm.AddCommand.Execute()
    }

    private func onLeftButtonTapped(node: BaseControlNode?)
    {
        SceneDelegate.Instance.flyoutController.openLeft()
    }

    @objc private func onRefresh()
    {
        self.vm.RefreshCommand.Execute()
    }
}
