import Foundation
import AsyncDisplayKit
import SharedAppCore

class iOSPage : ASDKViewController<ASDisplayNode>, IPage, UIGestureRecognizerDelegate
{
    var busyIndicatorNode: BusyIndicatorNode!
    var snackbarNode: SnackbarNode!
    var loggingService: ILoggingService? = nil
    private var nodesInitialized: Bool = false
    

    override init()
    {
        let rootNode = ASDisplayNode()
        rootNode.automaticallyManagesSubnodes = true
        super.init(node: rootNode)
        
        rootNode.backgroundColor = ColorConstants.BgColor.ToUIColor()
        
        rootNode.layoutSpecBlock = { [weak self] node, constrainedSize in
            
            guard let self = self else { return ASLayoutSpec() }
            
            //First initialize nodes
            if nodesInitialized == false
            {
                //make sure InitializeNodes is called only once
                nodesInitialized=true
                self.InitializeNodes()
            }
            
            //then use nodes and get layout
            return self.LayoutSpecOverride(node: node, constrainedSize: constrainedSize)
        }
        
        do
        {
            loggingService = try KoinResolver().GetLoggingService()
        }
        catch
        {
            print("KoinResolver().GetLoggingService() failed to resolve Logging service: \(error.localizedDescription)")
        }
    }

    required init?(coder: NSCoder)
    {
        fatalError("init(coder:) has not been implemented")
    }

    override func viewDidLoad()
    {
        addTouchHandler()
        
        busyIndicatorNode = BusyIndicatorNode()
        AddAndStretchNode(childNode: busyIndicatorNode)
        
        snackbarNode = SnackbarNode()
        AddAndStretchNode(childNode: snackbarNode)
    }
    
    // MARK: - Overridable hooks
    /// Subclasses override this to create and configure child nodes.
    func InitializeNodes()
    {
        // Default: do nothing
    }

    /// Subclasses override this to define layout.
    func LayoutSpecOverride(node: ASDisplayNode, constrainedSize: ASSizeRange) -> ASLayoutSpec
    {
        ASLayoutSpec()
    }
    
    func AddAndStretchNode(childNode: ASDisplayNode)
    {
        let parentView = self.node.view
        let childView = childNode.view
        parentView.addSubview(childView)
        childView.translatesAutoresizingMaskIntoConstraints = false

        NSLayoutConstraint.activate([
            childView.topAnchor.constraint(equalTo: parentView.topAnchor),
            childView.leadingAnchor.constraint(equalTo: parentView.leadingAnchor),
            childView.trailingAnchor.constraint(equalTo: parentView.trailingAnchor),
            childView.bottomAnchor.constraint(equalTo: parentView.bottomAnchor)
        ])
    }
    
    public func GetPageInsets() -> ASInsetLayoutSpec
    {
        let insetSpec = ASInsetLayoutSpec()
        insetSpec.insets = UIEdgeInsets(
            top: 0,
            left: CGFloat(NumConstants.PageHMargin),
            bottom: 0,
            right: CGFloat(NumConstants.PageHMargin)
        )
        return insetSpec
    }
    
   
    
    /// Implements hiding the keyboard when tapping anywhere outside input fields
    private func addTouchHandler()
    {
        // Create a tap gesture recognizer
        let tapGesture = UITapGestureRecognizer(target: self, action: #selector(endEditing))
        
        // Allow simultaneous recognition with other gestures
        tapGesture.delegate = self
        
        // Configure gesture behavior
        tapGesture.delaysTouchesBegan = false
        tapGesture.delaysTouchesEnded = false
        tapGesture.cancelsTouchesInView = false
        
        // Add to the main view (PlatformView in C#)
        view.addGestureRecognizer(tapGesture)
    }

    @objc private func endEditing()
    {
        view.endEditing(true)
    }
        
    // MARK: UIGestureRecognizerDelegate
    func gestureRecognizer(_ gestureRecognizer: UIGestureRecognizer, shouldRecognizeSimultaneouslyWith otherGestureRecognizer: UIGestureRecognizer) -> Bool
    {
        return true
    }
           
    func gestureRecognizer(_ gestureRecognizer: UIGestureRecognizer, shouldReceive touch: UITouch) -> Bool
    {
        return true
    }
    
    
    private var _viewModel: PageViewModel!
    var ViewModel: PageViewModel
    {
        get
        {
            guard let vm = _viewModel
            else
            {
                fatalError("ViewModel accessed before initialization")
            }
            return vm
        }
        set
        {
            _viewModel = newValue
        }
    }
}
