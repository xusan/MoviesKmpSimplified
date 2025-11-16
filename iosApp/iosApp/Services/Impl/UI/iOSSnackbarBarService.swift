import SharedAppCore

class iOSSnackbarBarService: ISnackbarService
{
    let PopupShowed = Event<SeverityType>()
    var pageNavigationService: IPageNavigationService!
    
    init()
    {
        pageNavigationService = try! KoinResolver().GetNavigationService()
    }

    func ShowError(message: String)
    {
        Show(message: message, severityType: SeverityType.error, duration: 3000)
    }

    func ShowInfo(message: String)
    {
        Show(message: message, severityType: SeverityType.info, duration: 3000)
    }
    
    func Show(message: String, severityType: SeverityType, duration: Int32)
    {
        PopupShowed.Invoke(value: severityType)
                
        if let page = pageNavigationService.GetCurrentPage() as? iOSLifecyclePage
        {
            page.snackbarNode.SetText(message, severityType);
            page.snackbarNode.Show();
        }
    }
}
