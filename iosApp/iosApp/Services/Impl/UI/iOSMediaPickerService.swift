//  Platform-specific implementation of IMediaPickerService for iOS.
//  This bridges Kotlin Multiplatform shared code with iOS UIKit APIs,
//  allowing the user to pick an image from gallery or take a photo using the camera.
//

import UIKit
import PhotosUI
import MobileCoreServices
import SharedAppCore // KMP module exposing IMediaPickerService, ILoggingService, etc.



/// iOS implementation of the shared IMediaPickerService KMP interface.
/// Provides async image picking from gallery or camera with optional resize/compression.
/// All public methods are marked `@MainActor` to ensure UIKit safety.
@MainActor class iOSMediaPickerService: NSObject, IMediaPickerService
{
    /// Cached presenting controller (used to show pickers)
    private weak var presentingVC: UIViewController?

    /// Used to suspend async call until user picks/cancels
    private var continuation: CheckedContinuation<MediaFile?, Error>?

    /// Shared logger resolved from Koin (shared dependency container)
    private let logger: ILoggingService

    /// Internal log tag prefix for consistency
    private let TAG = "IosMediaPickerService: "

    // MARK: - Initialization

    /// Default initializer. Attempts to resolve ILoggingService from Koin.
    /// `presentingVC` is resolved lazily from the current visible controller.
    override init()
    {
        self.presentingVC = nil
        self.logger = try! KoinResolver().GetLoggingService()
        super.init()
    }


    /// Opens gallery and returns selected image as `MediaFile`.
    func GetPhotoAsync(options: MediaOptions) async throws -> MediaFile?
    {
        do
        {
            let result = try await pickImage(source: .gallery, options: options)
            return result
        }
        catch
        {
            logger.LogError(error: error)
            return nil
        }
    }

    /// Opens camera and returns captured image as `MediaFile`.
    func TakePhotoAsync(options: MediaOptions) async throws -> MediaFile?
    {
        do
        {
            let result = try await pickImage(source: .camera, options: options)
            return result
        }
        catch
        {
            logger.LogError(error: error)
            return nil
        }
    }
    
    /// Attempts to get the top-most visible UIViewController.
    private func ensurePresentingController() -> UIViewController? {
        if presentingVC == nil
        {
            presentingVC = CurrentController.GetTopViewController()
        }
        if presentingVC == nil
        {
            logger.LogWarning(message: "No active view controller found to present picker.")
        }
        return presentingVC
    }

    /// Common image picking routine used by both camera and gallery.
    private func pickImage(source: MediaSource, options: MediaOptions) async throws -> MediaFile?
    {
        guard continuation == nil
        else
        {
            logger.LogWarning(message: "\(TAG)pickImage() - Media pick already in progress, ignoring duplicate request.")
            return nil
        }

        return try await withCheckedThrowingContinuation
        { (cont: CheckedContinuation<MediaFile?, Error>) in
            self.continuation = cont
            switch source
            {
                case .gallery:
                    presentGalleryPicker(options: options)
                case .camera:
                    presentCameraPicker(options: options)
                default:
                    logger.LogWarning(message: "\(TAG)pickImage() - Unknown MediaSource: \(source)")
                    cont.resume(returning: nil)
                    self.continuation = nil
            }
        }
    }

    /// Presents PHPickerViewController for selecting an image from the photo library.
    private func presentGalleryPicker(options: MediaOptions)
    {
        guard let vc = ensurePresentingController() else
        {
            logger.LogWarning(message: "\(TAG)presentGalleryPicker(): ensurePresentingController() returned null - this means it failed to get root ViewController.")
            continuation?.resume(throwing: NSError(domain: "NoActiveController for presentGalleryPicker()", code: -2))
            continuation = nil
            return
        }
        
        var config = PHPickerConfiguration(photoLibrary: .shared())
        config.selectionLimit = 1
        config.filter = .images
        
        let picker = PHPickerViewController(configuration: config)
        picker.delegate = self
        presentingVC?.present(picker, animated: true)
    }

    /// Presents UIImagePickerController for capturing a photo using the camera.
    private func presentCameraPicker(options: MediaOptions)
    {
        guard let vc = ensurePresentingController() else
        {
            logger.LogWarning(message: "\(TAG)presentCameraPicker(): ensurePresentingController() returned null - this means it failed to get root ViewController.")
            continuation?.resume(throwing: NSError(domain: "NoActiveController for presentCameraPicker()", code: -2))
            continuation = nil
            return
        }
        
        guard UIImagePickerController.isSourceTypeAvailable(.camera) else
        {
            logger.LogWarning(message: "\(TAG)Camera not available on this device.")
            continuation?.resume(throwing: NSError(domain: "CameraNotAvailable", code: -1))
            continuation = nil
            return
        }
        
        let picker = UIImagePickerController()
        picker.sourceType = .camera
        picker.delegate = self
        // Use UTType.image (new API, iOS 14+)
        if #available(iOS 14.0, *)
        {
            picker.mediaTypes = [UTType.image.identifier]
        }
        else
        {
            // Fallback for older OS (rare, iOS 13 and below)
            picker.mediaTypes = [kUTTypeImage as String]
        }
        presentingVC?.present(picker, animated: true)
    }

    /// Applies resize and compression based on MediaOptions.
    /// Returns a MediaFile with optional byte data if requested.
    private func processImage(_ image: UIImage, options: MediaOptions) -> MediaFile?
    {
        var processed = image

        if let maxW = options.maxWidth?.intValue, let maxH = options.maxHeight?.intValue
        {
            processed = resizeImage(image, maxWidth: CGFloat(maxW), maxHeight: CGFloat(maxH))
        }

        let quality = options.compress ? CGFloat(options.compressionQuality) / 100.0 : 1.0
        guard let data = processed.jpegData(compressionQuality: quality)
        else
        {
            logger.LogWarning(message: "Failed to compress image to JPEG.")
            return nil
        }

        let fileURL = saveToAppDirectory(data: data, ext: "jpg", saveToAppDir: options.saveToAppDirectory)
        let mime = "image/jpeg"
        let bytes = options.includeBytes ? data.toKotlinByteArray() : nil

        return MediaFile(FilePath: fileURL.path, MimeType: mime, ByteData: bytes)
    }

    /// Resizes UIImage maintaining aspect ratio.
    private func resizeImage(_ image: UIImage, maxWidth: CGFloat, maxHeight: CGFloat) -> UIImage
    {
        let size = image.size
        let ratio = min(maxWidth / size.width, maxHeight / size.height)
        if ratio >= 1
        {
            return image
        }

        let newSize = CGSize(width: size.width * ratio, height: size.height * ratio)
        UIGraphicsBeginImageContextWithOptions(newSize, false, 1.0)
        image.draw(in: CGRect(origin: .zero, size: newSize))
        let newImage = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()
        return newImage ?? image
    }

    /// Writes data to file (Documents or temporary directory).
    private func saveToAppDirectory(data: Data, ext: String, saveToAppDir: Bool) -> URL
    {
        let dir = saveToAppDir ? FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first! : FileManager.default.temporaryDirectory
        let filename = "IMG_\(Int(Date().timeIntervalSince1970)).\(ext)"
        let fileURL = dir.appendingPathComponent(filename)
        do
        {
            try data.write(to: fileURL)
        }
        catch
        {
            self.logger.LogError(error: error, message: "Failed to write image data to \(fileURL.path)")
        }
        return fileURL
    }
}

// MARK: - PHPicker Delegate
extension iOSMediaPickerService: PHPickerViewControllerDelegate
{
    func picker(_ picker: PHPickerViewController, didFinishPicking results: [PHPickerResult])
    {
        picker.dismiss(animated: true)
        guard let item = results.first, item.itemProvider.canLoadObject(ofClass: UIImage.self)
        else
        {
            logger.LogWarning(message: "No valid image selected from gallery.")
            continuation?.resume(returning: nil)
            continuation = nil
            return
        }

        item.itemProvider.loadObject(ofClass: UIImage.self)
        { [weak self] obj, err in
            DispatchQueue.main.async
            {
                if let error = err
                {
                    self?.logger.LogError(ex: KotlinThrowable(message: error.localizedDescription), message: "", handled: true)
                    self?.continuation?.resume(throwing: error)
                }
                else if let img = obj as? UIImage
                {
                    if let mediaFile = self?.processImage(img, options: MediaOptions())
                    {
                        self?.continuation?.resume(returning: mediaFile)
                    }
                    else
                    {
                        self?.logger.LogWarning(message: "Failed to process selected image.")
                        self?.continuation?.resume(returning: nil)
                    }
                }
                else
                {
                    self?.logger.LogWarning(message: "Loaded gallery item was not an image.")
                    self?.continuation?.resume(returning: nil)
                }
                self?.continuation = nil
            }
        }
    }
}


// MARK: - UIImagePicker Delegate
extension iOSMediaPickerService: UIImagePickerControllerDelegate, UINavigationControllerDelegate
{
    func imagePickerControllerDidCancel(_ picker: UIImagePickerController)
    {
        picker.dismiss(animated: true)
        logger.LogWarning(message: "User cancelled camera capture.")
        continuation?.resume(returning: nil)
        continuation = nil
    }

    func imagePickerController(_ picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey: Any])
    {
        picker.dismiss(animated: true)
        guard let image = info[.originalImage] as? UIImage
        else
        {
            logger.LogWarning(message: "Camera returned no image.")
            continuation?.resume(returning: nil)
            continuation = nil
            return
        }

        if let mediaFile = processImage(image, options: MediaOptions())
        {
            continuation?.resume(returning: mediaFile)
        }
        else
        {
            logger.LogWarning(message: "Camera image processing returned nil.")
            continuation?.resume(returning: nil)
        }

        continuation = nil
    }
}
