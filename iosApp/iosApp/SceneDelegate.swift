import UIKit
import SVGKit
import SharedAppCore

class SceneDelegate: UIResponder, UIWindowSceneDelegate {

    var window: UIWindow?
    var pageNavigationService: iOSPageNavigationController!
    private var sideViewController: SideMenuController!
    internal var flyoutController: FlyoutController!
    internal static var Instance: SceneDelegate!

    func scene(_ scene: UIScene, willConnectTo session: UISceneSession, options connectionOptions: UIScene.ConnectionOptions)
    {
        // Use this method to optionally configure and attach the UIWindow `window` to the provided UIWindowScene `scene`.
        // If using a storyboard, the `window` property will automatically be initialized and attached to the scene.
        // This delegate does not imply the connecting scene or session are new (see `application:configurationForConnectingSceneSession` instead).
        guard let windowScene = (scene as? UIWindowScene) else
        {
            print("Error: failed to resolve windowScene in SceneDelegate.scene() - debug this method to find out issue")
            return
        }
        
        //SVGKit.enableLogging()
        SceneDelegate.Instance = self
        //SetCulture()
        guard let appDelegate = UIApplication.shared.delegate as? AppDelegate else
        {
            print("Error: failed to resolve appDelegate in SceneDelegate.scene() - debug this method to find out issue")
            return
        }
        self.pageNavigationService = iOSPageNavigationController()
        let errorTrackingService = appDelegate.appErrorTracking
        let bootstrap = Bootstrap()
        bootstrap.RegisterTypes(self.pageNavigationService, errorTrackingService)
        
        let loggingService = try! KoinResolver().GetLoggingService()
        loggingService.Log(message: "####################################################- APPLICATION STARTED -####################################################")
        loggingService.Log(message: "AppDelegate.FinishedLaunching()")
       
               
       Task {
           await bootstrap.NavigateToPage(pageNavigationService)
           //setup attachment for error tracking service.
           // NOTE: The log file is only created after the first log entry.
           // A small delay is required to allow the buffer to flush to disk.
           try? await Task.sleep(nanoseconds: 300 * 1_000_000)
           appDelegate.appErrorTracking.SetupAttachment()
       }
        
        self.sideViewController = SideMenuController()
        self.sideViewController.InitializeNodes()
        
        self.flyoutController = FlyoutController(main: self.pageNavigationService, left: self.sideViewController, right: nil)
        
        let window = UIWindow(windowScene: windowScene)
        window.rootViewController = self.flyoutController
        window.makeKeyAndVisible()
        
        self.window = window
    }

    func sceneDidDisconnect(_ scene: UIScene) {
        // Called as the scene is being released by the system.
        // This occurs shortly after the scene enters the background, or when its session is discarded.
        // Release any resources associated with this scene that can be re-created the next time the scene connects.
        // The scene may re-connect later, as its session was not necessarily discarded (see `application:didDiscardSceneSessions` instead).
    }

    func sceneDidBecomeActive(_ scene: UIScene) {
        // Called when the scene has moved from an inactive state to an active state.
        // Use this method to restart any tasks that were paused (or not yet started) when the scene was inactive.
    }

    func sceneWillResignActive(_ scene: UIScene) {
        // Called when the scene will move from an active state to an inactive state.
        // This may occur due to temporary interruptions (ex. an incoming phone call).
    }

    func sceneWillEnterForeground(_ scene: UIScene) {
        // Called as the scene transitions from the background to the foreground.
        // Use this method to undo the changes made on entering the background.
    }

    func sceneDidEnterBackground(_ scene: UIScene) {
        // Called as the scene transitions from the foreground to the background.
        // Use this method to save data, release shared resources, and store enough scene-specific state information
        // to restore the scene back to its current state.
    }

    private func SetCulture()
    {
       UserDefaults.standard.set(["en_US"], forKey: "AppleLanguages")
       UserDefaults.standard.set("en_US", forKey: "AppleLocale")
       UserDefaults.standard.synchronize()
    }
    
    
}
