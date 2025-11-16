import SharedAppCore

class iOSLifecyclePage: iOSPage
{
    /// Indicates is wether page was navigated with animation.
    /// It is usefull when navigating back (pop) to check if we need to apply animation for navigation back,
    /// so it will be consistent with forward (push) navigation.
    var pushNavAnimated: Bool = true
    private var onApprearedSent: Bool = false
    public var Appeared = Event<IPage>()
    public var Disappeared = Event<IPage>()
 
    override func viewDidLoad()
    {
        super.viewDidLoad()
        
        loggingService?.Log(message: "\(type(of: self)).viewDidLoad() (from base)")
        
        self.ViewModel.PropertyChanged.AddListener(listener_: OnViewModelPropertyChanged)
    }

    public override func viewSafeAreaInsetsDidChange()
    {
        super.viewSafeAreaInsetsDidChange()
        
        loggingService?.Log(message: "\(type(of: self)).viewSafeAreaInsetsDidChange() (from base)")

        //safe area inset is initialized or changed so we need to force it to recalculate page layout
        if self.node != nil
        {
            self.node.setNeedsLayout()
        }
    }

    public override func viewWillAppear(_ animated: Bool)
    {
        loggingService?.Log(message: "\(type(of: self)).ViewWillAppear() (from base)")

        super.viewWillAppear(animated)

        self.ViewModel.OnAppearing()
    }

    public override func viewWillDisappear(_ animated: Bool)
    {
        loggingService?.Log(message: "\(type(of: self)).ViewWillDisappear() (from base)")

        super.viewWillDisappear(animated)

        self.ViewModel.OnDisappearing()
    }

    public override func viewDidAppear(_ animated: Bool)
    {
        loggingService?.Log(message: "\(type(of: self)).ViewDidAppear() (from base)")

        super.viewDidAppear(animated)
        
        self.Appeared.Invoke(value: self)
        self.SendOnAppeared()
    }

    public override func viewDidDisappear(_ animated: Bool)
    {
        loggingService?.Log(message: "\(type(of: self)).ViewDidDisappear() (from base)")

        super.viewDidDisappear(animated)
        
        self.Disappeared.Invoke(value: self)
    }

    private func SendOnAppeared()
    {
        if onApprearedSent //we want to send OnAppeared only once
        {
            return
        }

        onApprearedSent = true

        loggingService?.Log(message: "\(type(of: self)).SendOnAppeared() (from base)")

        self.ViewModel.OnAppeared()
    }

    func OnViewModelPropertyChanged(propertyName: NSString?)
    {
        guard let propertyName = propertyName as String? else { return }
        loggingService?.Log(message: "\(type(of: self)).ViewModel_PropertyChanged(\(propertyName))")

        if propertyName == #keyPath(ViewModel.BusyLoading).propertyName()
        {
            self.ShowLoadingIndicator(self.ViewModel.BusyLoading)
        }
    }

    public func ShowToasMsg(_ toastMessage: String, _ severity: SeverityType)
    {
        self.snackbarNode.SetText(toastMessage, severity)
        self.snackbarNode.Show()
    }

    func ShowLoadingIndicator(_ busyLoading: Bool)
    {
        if busyLoading
        {
            busyIndicatorNode.Show()
        }
        else
        {
            busyIndicatorNode.Close()
        }
    }

    public func OnBackBtnPressed()
    {
        self.ViewModel.BackCommand.Execute(param: nil)
    }
    
    public func Destroy()
    {
        loggingService?.Log(message: "\(type(of: self)).Destroy() (from base)")

        self.ViewModel.PropertyChanged.RemoveListener(listener_: OnViewModelPropertyChanged)
        self.ViewModel.Destroy()

//        if self.node != nil
//        {
//            self.node.Destroy()
//        }
    }
}
