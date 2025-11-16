import Foundation
import SharedAppCore

class iOSDirectoryService: IDirectoryService {
    func GetCacheDir() -> String {
        let paths = NSSearchPathForDirectoriesInDomains(.cachesDirectory, .userDomainMask, true)
        return paths.first ?? NSTemporaryDirectory()
    }

    func GetAppDataDir() -> String {
        let paths = NSSearchPathForDirectoriesInDomains(.documentDirectory, .userDomainMask, true)
        return paths.first ?? NSTemporaryDirectory()
    }

    func CreateDir(path: String) {
        let fileManager = FileManager.default

        if !IsExistDir(path: path) {
            do {
                try fileManager.createDirectory(
                    atPath: path,
                    withIntermediateDirectories: true,
                    attributes: nil
                )
                print("✅ Directory created at: \(path)")
            } catch {
                print("❌ Failed to create directory at \(path): \(error.localizedDescription)")
            }
        } else {
            print("ℹ️ Directory already exists: \(path)")
        }
    }

    func IsExistDir(path: String) -> Bool {
        var isDir: ObjCBool = false
        let exists = FileManager.default.fileExists(atPath: path, isDirectory: &isDir)
        return exists && isDir.boolValue
    }
}
