import UIKit

// MARK: - Configuration
public struct SlideMenuOptions {
    public static var leftViewWidth: CGFloat = 250.0
    public static var leftBezelWidth: CGFloat = 16.0
    public static var contentViewScale: CGFloat = 1.0
    public static var contentViewOpacity: CGFloat = 0.5
    public static var animationType: SlideAnimation = .makeScale
    public static var shadowOpacity: Float = 0.0
    public static var shadowRadius: CGFloat = 0.0
    public static var shadowOffset: CGSize = .zero

    public static var panFromBezel: Bool = true
    public static var animationDuration: TimeInterval = 0.15
    public static var rightViewWidth: CGFloat = 270.0
    public static var rightBezelWidth: CGFloat = 16.0
    public static var rightPanFromBezel: Bool = true
    public static var hideStatusBar: Bool = true
    public static var pointOfNoReturnWidth: CGFloat = 44.0
    public static var simultaneousGestureRecognizers: Bool = true
    public static var opacityViewBackgroundColor: UIColor = .black
}

// MARK: - Enums & Helper types
public enum SlideAnimation { case contentViewDrag, makeScale, `default` }
public enum SlideAction { case open, close }
public enum TrackAction {
    case leftTapOpen, leftTapClose, leftFlickOpen, leftFlickClose
    case rightTapOpen, rightTapClose, rightFlickOpen, rightFlickClose
}

fileprivate struct PanInfo { var action: SlideAction = .close; var shouldBounce = false; var velocity: CGFloat = 0 }

fileprivate struct LeftPanState {
    static var frameAtStartOfPan: CGRect = .zero
    static var startPointOfPan: CGPoint = .zero
    static var wasOpenAtStartOfPan: Bool = false
    static var wasHiddenAtStartOfPan: Bool = false
    static var lastState: UIGestureRecognizer.State = .ended
}

fileprivate struct RightPanState {
    static var frameAtStartOfPan: CGRect = .zero
    static var startPointOfPan: CGPoint = .zero
    static var wasOpenAtStartOfPan: Bool = false
    static var wasHiddenAtStartOfPan: Bool = false
    static var lastState: UIGestureRecognizer.State = .ended
}

// MARK: - Controller
public class FlyoutController: UIViewController, UIGestureRecognizerDelegate {

    // MARK: Callbacks (converted from C# Actions)
    public var leftWillOpen: (() -> Void)?
    public var leftDidOpen: (() -> Void)?
    public var leftWillClose: (() -> Void)?
    public var leftDidClose: (() -> Void)?
    public var rightWillOpen: (() -> Void)?
    public var rightDidOpen: (() -> Void)?
    public var rightWillClose: (() -> Void)?
    public var rightDidClose: (() -> Void)?

    // MARK: Views
    public var opacityView = UIView()
    public var mainContainerView = UIView()
    public var leftContainerView = UIView()
    public var rightContainerView = UIView()

    // MARK: Child controllers
    public var mainViewController: UIViewController?
    public var leftViewController: UIViewController?
    public var rightViewController: UIViewController?

    // MARK: Gestures
    public var leftPanGesture: UIPanGestureRecognizer?
    public var rightPanGesture: UIPanGestureRecognizer?
    public var leftTapGesture: UITapGestureRecognizer?
    public var rightTapGesture: UITapGestureRecognizer?

    // MARK: Bridge property
    public var animationType: SlideAnimation {
        get { SlideMenuOptions.animationType }
        set { SlideMenuOptions.animationType = newValue }
    }

    // MARK: Inits
    public init(main: UIViewController? = nil, left: UIViewController? = nil, right: UIViewController? = nil) {
        self.mainViewController = main
        self.leftViewController = left
        self.rightViewController = right
        super.init(nibName: nil, bundle: nil)
        initView()
    }

    public required init?(coder: NSCoder) {
        super.init(coder: coder)
    }

    // MARK: Lifecycle
    public override func viewDidLoad() {
        super.viewDidLoad()
        if mainViewController != nil { /* no-op, parity */ }
        edgesForExtendedLayout = []
    }

    public override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        mainViewController?.beginAppearanceTransition(true, animated: animated)
        mainViewController?.endAppearanceTransition()
    }

    public override func awakeFromNib() {
        super.awakeFromNib()
        initView()
    }

    public override var supportedInterfaceOrientations: UIInterfaceOrientationMask {
        if let main = mainViewController { return main.supportedInterfaceOrientations }
        return .all
    }

    public override var shouldAutorotate: Bool {
        if let main = mainViewController { return main.shouldAutorotate }
        return false
    }

    public override func viewWillLayoutSubviews() {
        super.viewWillLayoutSubviews()
        setUpViewController(targetView: mainContainerView, controller: mainViewController)
        setUpViewController(targetView: leftContainerView, controller: leftViewController)
        setUpViewController(targetView: rightContainerView, controller: rightViewController)
    }

    // MARK: Setup
    private func initView() {
        mainContainerView = UIView(frame:view.bounds)
        mainContainerView.backgroundColor = .clear
        mainContainerView.autoresizingMask = [.flexibleWidth, .flexibleHeight]
        view.insertSubview(mainContainerView, at: 0)

        var opacityFrame = view.bounds
        let opacityOffset: CGFloat = 0
        opacityFrame.origin.y += opacityOffset
        opacityFrame.size.height -= opacityOffset
        opacityView = UIView(frame: opacityFrame)
        opacityView.backgroundColor = SlideMenuOptions.opacityViewBackgroundColor
        opacityView.autoresizingMask = [.flexibleWidth, .flexibleHeight]
        opacityView.layer.opacity = 0
        view.insertSubview(opacityView, at: 1)

        if leftViewController != nil {
            var leftFrame = view.bounds
            leftFrame.size.width = SlideMenuOptions.leftViewWidth
            leftFrame.origin.x = leftMinOrigin()
            let leftOffset: CGFloat = 0
            leftFrame.origin.y += leftOffset
            leftFrame.size.height -= leftOffset
            leftContainerView = UIView(frame: leftFrame)
            leftContainerView.backgroundColor = .clear
            leftContainerView.autoresizingMask = [.flexibleHeight]
            view.insertSubview(leftContainerView, at: 2)
            addLeftGestures()
        }

        if rightViewController != nil {
            var rightFrame = view.bounds
            rightFrame.size.width = SlideMenuOptions.rightViewWidth
            rightFrame.origin.x = rightMinOrigin()
            let rightOffset: CGFloat = 0
            rightFrame.origin.y += rightOffset
            rightFrame.size.height -= rightOffset
            rightContainerView = UIView(frame: rightFrame)
            rightContainerView.backgroundColor = .clear
            rightContainerView.autoresizingMask = [.flexibleHeight]
            view.insertSubview(rightContainerView, at: 3)
            addRightGestures()
        }
    }

    public override func viewWillTransition(to size: CGSize, with coordinator: UIViewControllerTransitionCoordinator) {
        super.viewWillTransition(to: size, with: coordinator)

        mainContainerView.transform = .identity
        leftContainerView.isHidden = true
        rightContainerView.isHidden = true

        coordinator.animate(alongsideTransition: { _ in }, completion: { _ in
            self.closeLeftNonAnimation()
            self.closeRightNonAnimation()
            self.leftContainerView.isHidden = false
            self.rightContainerView.isHidden = false

            if self.leftPanGesture != nil && self.leftTapGesture != nil {
                self.removeLeftGestures()
                self.addLeftGestures()
            }
            if self.rightPanGesture != nil && self.rightTapGesture != nil {
                self.removeRightGestures()
                self.addRightGestures()
            }
        })
    }

    // MARK: Memory
    public override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
    }

    // MARK: Public open/close
    public func openLeft() {
        guard leftViewController != nil else { return }
        leftWillOpen?()
        setOpenWindowLevel()
        leftViewController?.beginAppearanceTransition(isLeftHidden(), animated: true)
        openLeft(withVelocity: 0)
        track(.leftTapOpen)
    }

    public func closeLeft() {
        guard leftViewController != nil else { return }
        leftWillClose?()
        leftViewController?.beginAppearanceTransition(isLeftHidden(), animated: true)
        closeLeft(withVelocity: 0)
        setOpenWindowLevel()
    }

    public func openRight() {
        guard rightViewController != nil else { return }
        rightWillOpen?()
        setOpenWindowLevel()
        rightViewController?.beginAppearanceTransition(isRightHidden(), animated: true)
        openRight(withVelocity: 0)
        track(.rightTapOpen)
    }

    public func closeRight() {
        guard rightViewController != nil else { return }
        rightWillClose?()
        rightViewController?.beginAppearanceTransition(isRightHidden(), animated: true)
        closeRight(withVelocity: 0)
        setOpenWindowLevel()
    }

    // MARK: Gestures
    public func addLeftGestures() {
        guard leftViewController != nil else { return }
        if leftPanGesture == nil {
            let pan = UIPanGestureRecognizer(target: self, action: #selector(handleLeftPanGesture(_:)))
            pan.delegate = self
            view.addGestureRecognizer(pan)
            leftPanGesture = pan
        }
        if leftTapGesture == nil {
            let tap = UITapGestureRecognizer(target: self, action: #selector(toggleLeft))
            tap.delegate = self
            view.addGestureRecognizer(tap)
            leftTapGesture = tap
        }
    }

    public func addRightGestures() {
        guard rightViewController != nil else { return }
        if rightPanGesture == nil {
            let pan = UIPanGestureRecognizer(target: self, action: #selector(handleRightPanGesture(_:)))
            pan.delegate = self
            view.addGestureRecognizer(pan)
            rightPanGesture = pan
        }
        if rightTapGesture == nil {
            let tap = UITapGestureRecognizer(target: self, action: #selector(toggleRight))
            tap.delegate = self
            view.addGestureRecognizer(tap)
            rightTapGesture = tap
        }
    }

    public func removeLeftGestures() {
        if let g = leftPanGesture { view.removeGestureRecognizer(g); leftPanGesture = nil }
        if let g = leftTapGesture { view.removeGestureRecognizer(g); leftTapGesture = nil }
    }

    public func removeRightGestures() {
        if let g = rightPanGesture { view.removeGestureRecognizer(g); rightPanGesture = nil }
        if let g = rightTapGesture { view.removeGestureRecognizer(g); rightTapGesture = nil }
    }

    // MARK: Helpers
    private func leftMinOrigin() -> CGFloat { -SlideMenuOptions.leftViewWidth }
    private func rightMinOrigin() -> CGFloat { view.bounds.width }

    public func isTargetViewController() -> Bool { true }
    public func track(_ action: TrackAction) { /* override if needed */ }

    // MARK: Pan handlers
    @objc public func handleLeftPanGesture(_ pan: UIPanGestureRecognizer) {
        guard isTargetViewController(), !isRightOpen() else { return }
        switch pan.state {
        case .began:
            guard LeftPanState.lastState == .ended || LeftPanState.lastState == .cancelled || LeftPanState.lastState == .failed else { return }
            if isLeftHidden() { leftWillOpen?() } else { leftWillClose?() }
            LeftPanState.frameAtStartOfPan = leftContainerView.frame
            LeftPanState.startPointOfPan = pan.location(in: view)
            LeftPanState.wasOpenAtStartOfPan = isLeftOpen()
            LeftPanState.wasHiddenAtStartOfPan = isLeftHidden()
            leftViewController?.beginAppearanceTransition(LeftPanState.wasHiddenAtStartOfPan, animated: true)
            addShadow(to: leftContainerView)
            setOpenWindowLevel()
        case .changed:
            guard LeftPanState.lastState == .began || LeftPanState.lastState == .changed else { return }
            let translation = pan.translation(in: pan.view)
            leftContainerView.frame = applyLeftTranslation(translation, to: LeftPanState.frameAtStartOfPan)
            applyLeftOpacity()
            applyLeftContentViewScale()
        case .ended, .cancelled:
            guard LeftPanState.lastState == .changed else { setCloseWindowLevel(); return }
            let velocity = pan.velocity(in: pan.view)
            let info = panLeftResultInfo(for: velocity)
            if info.action == .open {
                if !LeftPanState.wasHiddenAtStartOfPan { leftViewController?.beginAppearanceTransition(true, animated: true) }
                openLeft(withVelocity: info.velocity)
                track(.leftFlickOpen)
            } else {
                if !LeftPanState.wasHiddenAtStartOfPan { leftViewController?.beginAppearanceTransition(false, animated: true) }
                closeLeft(withVelocity: info.velocity)
                setCloseWindowLevel()
                track(.leftFlickClose)
            }
        default: break
        }
        LeftPanState.lastState = pan.state
    }

    @objc public func toggleLeft() {
        if isLeftOpen() {
            closeLeft(); setCloseWindowLevel(); track(.leftTapClose)
        } else { openLeft() }
    }

    @objc public func handleRightPanGesture(_ pan: UIPanGestureRecognizer) {
        guard isTargetViewController(), !isLeftOpen() else { return }
        switch pan.state {
        case .began:
            guard RightPanState.lastState == .ended || RightPanState.lastState == .cancelled || RightPanState.lastState == .failed else { return }
            if isRightHidden() { rightWillOpen?() } else { rightWillClose?() }
            RightPanState.frameAtStartOfPan = rightContainerView.frame
            RightPanState.startPointOfPan = pan.location(in: view)
            RightPanState.wasOpenAtStartOfPan = isRightOpen()
            RightPanState.wasHiddenAtStartOfPan = isRightHidden()
            rightViewController?.beginAppearanceTransition(RightPanState.wasHiddenAtStartOfPan, animated: true)
            addShadow(to: rightContainerView)
            setOpenWindowLevel()
        case .changed:
            guard RightPanState.lastState == .began || RightPanState.lastState == .changed else { return }
            let translation = pan.translation(in: pan.view)
            rightContainerView.frame = applyRightTranslation(translation, to: RightPanState.frameAtStartOfPan)
            applyRightOpacity()
            applyRightContentViewScale()
        case .ended, .cancelled:
            guard RightPanState.lastState == .changed else { setCloseWindowLevel(); return }
            let velocity = pan.velocity(in: pan.view)
            let info = panRightResultInfo(for: velocity)
            if info.action == .open {
                if !RightPanState.wasHiddenAtStartOfPan { rightViewController?.beginAppearanceTransition(true, animated: true) }
                openRight(withVelocity: info.velocity)
                track(.rightFlickOpen)
            } else {
                if !RightPanState.wasHiddenAtStartOfPan { rightViewController?.beginAppearanceTransition(false, animated: true) }
                closeRight(withVelocity: info.velocity)
                setCloseWindowLevel()
                track(.rightFlickClose)
            }
        default: break
        }
        RightPanState.lastState = pan.state
    }

    @objc public func toggleRight() {
        if isRightOpen() {
            closeRight(); setCloseWindowLevel(); track(.rightTapClose)
        } else { openRight() }
    }

    // MARK: State queries
    public func isLeftOpen() -> Bool {
        return leftViewController != nil && leftContainerView.frame.minX == 0
    }

    public func isLeftHidden() -> Bool {
        return leftContainerView.frame.minX <= leftMinOrigin()
    }

    public func isRightOpen() -> Bool {
        return rightViewController != nil && rightContainerView.frame.minX == view.bounds.width - rightContainerView.frame.size.width
    }

    public func isRightHidden() -> Bool {
        return rightContainerView.frame.minX >= view.bounds.width
    }

    // MARK: Open/Close with velocity
    public func openLeft(withVelocity velocity: CGFloat) {
        let xOrigin = leftContainerView.frame.minX
        let finalX: CGFloat = 0
        var frame = leftContainerView.frame
        frame.origin.x = finalX
        var duration = SlideMenuOptions.animationDuration
        if velocity != 0 {
            let d = abs(xOrigin - finalX) / abs(velocity)
            duration = max(0.1, min(1.0, TimeInterval(d)))
        }
        addShadow(to: leftContainerView)
        UIView.animate(withDuration: duration, delay: 0, options: [], animations: {
            self.leftContainerView.frame = frame
            self.opacityView.layer.opacity = Float(SlideMenuOptions.contentViewOpacity)
            switch SlideMenuOptions.animationType {
            case .contentViewDrag:
                self.mainContainerView.transform = CGAffineTransform(translationX: SlideMenuOptions.leftViewWidth, y: 0)
            case .makeScale:
                self.mainContainerView.transform = CGAffineTransform(scaleX: SlideMenuOptions.contentViewScale, y: SlideMenuOptions.contentViewScale)
            default: break
            }
        }, completion: { _ in
            self.disableContentInteraction()
            self.leftViewController?.endAppearanceTransition()
            self.leftDidOpen?()
        })
    }

    public func openRight(withVelocity velocity: CGFloat) {
        let xOrigin = rightContainerView.frame.minX
        let finalX: CGFloat = view.bounds.width - rightContainerView.frame.size.width
        var frame = rightContainerView.frame
        frame.origin.x = finalX
        var duration = SlideMenuOptions.animationDuration
        if velocity != 0 {
            let d = abs(xOrigin - view.bounds.width) / abs(velocity)
            duration = max(0.1, min(1.0, TimeInterval(d)))
        }
        addShadow(to: rightContainerView)
        UIView.animate(withDuration: duration, delay: 0, options: [], animations: {
            self.rightContainerView.frame = frame
            self.opacityView.layer.opacity = Float(SlideMenuOptions.contentViewOpacity)
            switch SlideMenuOptions.animationType {
            case .contentViewDrag:
                self.mainContainerView.transform = CGAffineTransform(translationX: -SlideMenuOptions.rightViewWidth, y: 0)
            case .makeScale:
                self.mainContainerView.transform = CGAffineTransform(scaleX: SlideMenuOptions.contentViewScale, y: SlideMenuOptions.contentViewScale)
            default: break
            }
        }, completion: { _ in
            self.disableContentInteraction()
            self.rightViewController?.endAppearanceTransition()
            self.rightDidOpen?()
        })
    }

    public func closeLeft(withVelocity velocity: CGFloat) {
        let xOrigin = leftContainerView.frame.minX
        let finalX: CGFloat = leftMinOrigin()
        var frame = leftContainerView.frame
        frame.origin.x = finalX
        var duration = SlideMenuOptions.animationDuration
        if velocity != 0 {
            let d = abs(xOrigin - finalX) / abs(velocity)
            duration = max(0.1, min(1.0, TimeInterval(d)))
        }
        UIView.animate(withDuration: duration, delay: 0, options: [], animations: {
            self.leftContainerView.frame = frame
            self.opacityView.layer.opacity = 0
            if SlideMenuOptions.animationType == .makeScale {
                self.mainContainerView.transform = .identity
            }
        }, completion: { _ in
            self.removeShadow(from: self.leftContainerView)
            self.enableContentInteraction()
            self.leftViewController?.endAppearanceTransition()
            self.leftDidClose?()
        })
    }

    public func closeRight(withVelocity velocity: CGFloat) {
        let xOrigin = rightContainerView.frame.minX
        let finalX: CGFloat = view.bounds.width
        var frame = rightContainerView.frame
        frame.origin.x = finalX
        var duration = SlideMenuOptions.animationDuration
        if velocity != 0 {
            let d = abs(xOrigin - view.bounds.width) / abs(velocity)
            duration = max(0.1, min(1.0, TimeInterval(d)))
        }
        UIView.animate(withDuration: duration, delay: 0, options: [], animations: {
            self.rightContainerView.frame = frame
            self.opacityView.layer.opacity = 0
            if SlideMenuOptions.animationType == .makeScale {
                self.mainContainerView.transform = .identity
            }
        }, completion: { _ in
            self.removeShadow(from: self.rightContainerView)
            self.enableContentInteraction()
            self.rightViewController?.endAppearanceTransition()
            self.rightDidClose?()
        })
    }

    // MARK: Change controllers & widths
    public func changeMainViewController(_ controller: UIViewController?, close: Bool) {
        removeViewController(mainViewController)
        mainViewController = controller
        setUpViewController(targetView: mainContainerView, controller: mainViewController)
        if close { closeLeft(); closeRight() }
    }

    public func changeLeftViewController(_ controller: UIViewController?, close: Bool) {
        removeViewController(leftViewController)
        leftViewController = controller
        setUpViewController(targetView: leftContainerView, controller: leftViewController)
        if close { closeLeft() }
    }

    public func changeRightViewController(_ controller: UIViewController?, close: Bool) {
        removeViewController(rightViewController)
        rightViewController = controller
        setUpViewController(targetView: rightContainerView, controller: rightViewController)
        if close { closeRight() }
    }

    public func changeLeftViewWidth(_ width: CGFloat) {
        SlideMenuOptions.leftViewWidth = width
        var frame = view.bounds
        frame.size.width = width
        frame.origin.x = leftMinOrigin()
        frame.origin.y += 0
        frame.size.height -= 0
        leftContainerView.frame = frame
    }

    public func changeRightViewWidth(_ width: CGFloat) {
        SlideMenuOptions.rightBezelWidth = width
        var frame = view.bounds
        frame.size.width = width
        frame.origin.x = rightMinOrigin()
        frame.origin.y += 0
        frame.size.height -= 0
        rightContainerView.frame = frame
    }

    // MARK: Pan math
    private func panLeftResultInfo(for velocity: CGPoint) -> PanInfo {
        let threshold: CGFloat = 1000
        let pointOfNoReturn = floor(leftMinOrigin()) + SlideMenuOptions.pointOfNoReturnWidth
        let leftOrigin = leftContainerView.frame.minX
        var info = PanInfo()
        info.action = leftOrigin <= pointOfNoReturn ? .close : .open
        if velocity.x >= threshold { info.action = .open; info.velocity = velocity.x }
        else if velocity.x <= -threshold { info.action = .close; info.velocity = velocity.x }
        return info
    }

    private func panRightResultInfo(for velocity: CGPoint) -> PanInfo {
        let threshold: CGFloat = -1000
        let pointOfNoReturn = floor(view.bounds.width) - SlideMenuOptions.pointOfNoReturnWidth
        let rightOrigin = rightContainerView.frame.minX
        var info = PanInfo()
        info.action = rightOrigin >= pointOfNoReturn ? .close : .open
        if velocity.x <= threshold { info.action = .open; info.velocity = velocity.x }
        else if velocity.x >= -threshold { info.action = .close; info.velocity = velocity.x }
        return info
    }

    private func applyLeftTranslation(_ translation: CGPoint, to frame: CGRect) -> CGRect {
        var newOrigin = frame.minX + translation.x
        let minO = leftMinOrigin(); let maxO: CGFloat = 0
        if newOrigin < minO { newOrigin = minO } else if newOrigin > maxO { newOrigin = maxO }
        var f = frame; f.origin.x = newOrigin; return f
    }

    private func applyRightTranslation(_ translation: CGPoint, to frame: CGRect) -> CGRect {
        var newOrigin = frame.minX + translation.x
        let minO = rightMinOrigin(); let maxO = rightMinOrigin() - rightContainerView.frame.width
        if newOrigin > minO { newOrigin = minO } else if newOrigin < maxO { newOrigin = maxO }
        var f = frame; f.origin.x = newOrigin; return f
    }

    private func openedLeftRatio() -> CGFloat {
        let width = leftContainerView.frame.width
        let current = leftContainerView.frame.minX - leftMinOrigin()
        return current / width
    }

    private func openedRightRatio() -> CGFloat {
        let width = rightContainerView.frame.width
        let current = rightContainerView.frame.minX
        return -(current - view.bounds.width) / width
    }

    private func applyLeftOpacity() {
        let ratio = openedLeftRatio()
        opacityView.layer.opacity = Float(SlideMenuOptions.contentViewOpacity * ratio)
    }

    private func applyRightOpacity() {
        let ratio = openedRightRatio()
        opacityView.layer.opacity = Float(SlideMenuOptions.contentViewOpacity * ratio)
    }

    private func applyLeftContentViewScale() {
        let ratio = openedLeftRatio()
        let scale = 1.0 - ((1.0 - SlideMenuOptions.contentViewScale) * ratio)
        let drag = SlideMenuOptions.leftViewWidth + leftContainerView.frame.minX
        switch SlideMenuOptions.animationType {
        case .contentViewDrag:
            mainContainerView.transform = CGAffineTransform(translationX: drag, y: 0)
        case .makeScale:
            mainContainerView.transform = CGAffineTransform(scaleX: scale, y: scale)
        default: break
        }
    }

    private func applyRightContentViewScale() {
        let ratio = openedRightRatio()
        let scale = 1.0 - ((1.0 - SlideMenuOptions.contentViewScale) * ratio)
        let drag = rightContainerView.frame.minX - mainContainerView.frame.width
        switch SlideMenuOptions.animationType {
        case .contentViewDrag:
            mainContainerView.transform = CGAffineTransform(translationX: drag, y: 0)
        case .makeScale:
            mainContainerView.transform = CGAffineTransform(scaleX: scale, y: scale)
        default: break
        }
    }

    private func addShadow(to view: UIView) {
        view.layer.masksToBounds = false
        view.layer.shadowOffset = SlideMenuOptions.shadowOffset
        view.layer.shadowOpacity = SlideMenuOptions.shadowOpacity
        view.layer.shadowRadius = SlideMenuOptions.shadowRadius
        view.layer.shadowPath = UIBezierPath(rect: view.bounds).cgPath
    }

    private func removeShadow(from view: UIView) {
        view.layer.masksToBounds = true
        mainContainerView.layer.opacity = 1.0
    }

    private func removeContentOpacity() { opacityView.layer.opacity = 0 }
    private func addContentOpacity() { opacityView.layer.opacity = Float(SlideMenuOptions.contentViewOpacity) }

    private func disableContentInteraction() { mainContainerView.isUserInteractionEnabled = false }
    private func enableContentInteraction() { mainContainerView.isUserInteractionEnabled = true }

    private func setOpenWindowLevel() {
        guard SlideMenuOptions.hideStatusBar else { return }
        DispatchQueue.main.async {
            if let window = UIApplication.shared.keyWindow {
                window.windowLevel = UIWindow.Level.statusBar + 1
            }
        }
    }

    private func setCloseWindowLevel() {
        guard SlideMenuOptions.hideStatusBar else { return }
        DispatchQueue.main.async {
            if let window = UIApplication.shared.keyWindow {
                window.windowLevel = .normal
            }
        }
    }

    private func setUpViewController(targetView: UIView, controller: UIViewController?) {
        guard let c = controller else { return }
        addChild(c)
        c.view.frame = targetView.bounds
        targetView.addSubview(c.view)
        c.didMove(toParent: self)
    }

    private func removeViewController(_ controller: UIViewController?) {
        guard let vc = controller else { return }
        vc.view.layer.removeAllAnimations()
        vc.willMove(toParent: nil)
        vc.view.removeFromSuperview()
        vc.removeFromParent()
    }

    public func closeLeftNonAnimation() {
        setCloseWindowLevel()
        var frame = leftContainerView.frame
        frame.origin.x = leftMinOrigin()
        leftContainerView.frame = frame
        opacityView.layer.opacity = 0
        if SlideMenuOptions.animationType == .makeScale { mainContainerView.transform = .identity }
        removeShadow(from: leftContainerView)
        enableContentInteraction()
    }

    public func closeRightNonAnimation() {
        setCloseWindowLevel()
        var frame = rightContainerView.frame
        frame.origin.x = view.bounds.width
        rightContainerView.frame = frame
        opacityView.layer.opacity = 0
        if SlideMenuOptions.animationType == .makeScale { mainContainerView.transform = .identity }
        removeShadow(from: rightContainerView)
        enableContentInteraction()
    }

    // MARK: UIGestureRecognizerDelegate
    public func gestureRecognizer(_ gestureRecognizer: UIGestureRecognizer, shouldReceive touch: UITouch) -> Bool {
        let point = touch.location(in: view)
        if gestureRecognizer === leftPanGesture { return slideLeft(for: gestureRecognizer, point: point) }
        else if gestureRecognizer === rightPanGesture { return slideRight(for: gestureRecognizer, point: point) }
        else if gestureRecognizer === leftTapGesture { return isLeftOpen() && !isPointContainedWithinLeftRect(point) }
        else if gestureRecognizer === rightTapGesture { return isRightOpen() && !isPointContainedWithinRightRect(point) }
        return true
    }

    public func gestureRecognizer(_ gestureRecognizer: UIGestureRecognizer, shouldRecognizeSimultaneouslyWith otherGestureRecognizer: UIGestureRecognizer) -> Bool {
        return SlideMenuOptions.simultaneousGestureRecognizers
    }

    private func slideLeft(for gesture: UIGestureRecognizer, point: CGPoint) -> Bool {
        return isLeftOpen() || (SlideMenuOptions.panFromBezel && isLeftPointContainedWithinBezelRect(point))
    }

    private func slideRight(for gesture: UIGestureRecognizer, point: CGPoint) -> Bool {
        return isRightOpen() || (SlideMenuOptions.rightPanFromBezel && isRightPointContainedWithinBezelRect(point))
    }

    private func isLeftPointContainedWithinBezelRect(_ point: CGPoint) -> Bool {
        let bezelWidth = SlideMenuOptions.leftBezelWidth
        let leftBezel = CGRect(x: 0, y: 0, width: bezelWidth, height: view.bounds.height)
        return leftBezel.contains(point)
    }

    private func isRightPointContainedWithinBezelRect(_ point: CGPoint) -> Bool {
        let rightBezelWidth = SlideMenuOptions.rightBezelWidth
        
        let bezelWidth = view.bounds.width - rightBezelWidth
        var rightBezel = CGRect(x: view.bounds.width - rightBezelWidth, y: 0, width: rightBezelWidth, height: view.bounds.height)
        return rightBezel.contains(point)
    }

    private func isPointContainedWithinLeftRect(_ point: CGPoint) -> Bool { leftContainerView.frame.contains(point) }
    private func isPointContainedWithinRightRect(_ point: CGPoint) -> Bool { rightContainerView.frame.contains(point) }
}
