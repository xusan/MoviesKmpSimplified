import SharedAppCore
import SwiftyBeaver



class iOSFileLogger: IFileLogger
{
    
    private var directoryService: IDirectoryService!
    private var zipService: IZipService!
    private let log = SwiftyBeaver.self
    private var currentLogPath: String = ""
    private var sessionDir: URL!
    private var logRoot: URL!
    private let TAG = "iOSFileLogger: "

    init()
    {
        do
        {
            directoryService = try KoinResolver().GetDirectoryService()
            zipService = try KoinResolver().GetZipService()
            let logFolder = GetLogsFolder()
            guard !logFolder.isEmpty else
            {
                print("\(TAG)ERROR Failed to get GetLogsFolder()")
                return
            }
            logRoot = URL(fileURLWithPath: logFolder)
            // 🗓 FOLDER PER DAY
            let dateFormatter = DateFormatter()
            dateFormatter.dateFormat = "yyyy-MM-dd"
            let dayFolderName = dateFormatter.string(from: Date())
            sessionDir = logRoot.appendingPathComponent(dayFolderName)
            try FileManager.default.createDirectory(at: sessionDir, withIntermediateDirectories: true)

            // 🕒 FILE PER SESSION
            dateFormatter.dateFormat = "yyyy-MM-dd_HH-mm-ss"
            currentLogPath = sessionDir.appendingPathComponent("session_\(dateFormatter.string(from: Date())).log").path

            let file = FileDestination()
            file.logFileURL = URL(fileURLWithPath: currentLogPath)

            // 💬 Customize formatting (A/B/C choices below)
            // A) Exact Android "%msg%n"
            file.format = "$M"

            let isAdded = log.addDestination(file)
            print("\(TAG)addDestination() returned: \(isAdded)")

            cleanupOldLogs()
        }
        catch
        {
            print("\(TAG)\(error.localizedDescription)")
        }
    }

    func Init()
    {
        
    }
    
    func Info(message: String)
    {
        log.info(message)
    }

    func Warn(message: String)
    {
        log.warning(message)
    }

    func Error(message: String)
    {
        log.error(message)
    }
    
    func GetCompressedLogsSync(getOnlyLastSession: Bool) async throws -> KotlinByteArray?
    {
        let data = try await getCompressedLogsInternal(getOnlyLastSession: getOnlyLastSession)
        if data != nil
        {
            return data?.toKotlinByteArray()
        }
        else
        {
            return nil
        }
    }

    func GetLogListAsync() async throws -> [String]
    {
        guard let content = try? String(contentsOfFile: currentLogPath)
        else
        {
            return []
        }
        let lines = content.split(separator: "\n").map(String.init)
        return Array(lines.suffix(100))
    }
    
    func GetLogsFolder() -> String
    {
        do
        {
            let root = directoryService.GetAppDataDir()
            let folder = URL(fileURLWithPath: root).appendingPathComponent("SwiftyBeaver")
            try FileManager.default.createDirectory(at: folder, withIntermediateDirectories: true)
            return folder.path
        }
        catch
        {
            print("\(TAG): GetLogsFolder() Failed: \(error.localizedDescription)")
            return ""
        }
    }
    
    func GetCurrentLogFileName() -> String
    {
        return currentLogPath
    }
    
    public func getCompressedLogsInternal(getOnlyLastSession: Bool) async throws -> Data?
    {
        let tempZipPath = NSTemporaryDirectory() + "logs.zip"
        let zipURL = URL(fileURLWithPath: tempZipPath)

        do
        {
            // Remove old zip if exists
            if FileManager.default.fileExists(atPath: zipURL.path)
            {
                try FileManager.default.removeItem(at: zipURL)
            }

            if getOnlyLastSession
            {
                try await zipService.CreateFromFileAsync(filePath: currentLogPath, zipPath: tempZipPath)
            }
            else
            {
                try await zipService.CreateFromDirectoryAsync(fileDir: logRoot.path, zipPath: tempZipPath)
            }

            let data = try Data(contentsOf: zipURL)
            return data
        }
        catch
        {
            print("\(TAG) GetCompressedLogsAsyncSwift() failed: \(error)")
            return nil
        }
    }

    private func cleanupOldLogs()
    {
        let fileManager = FileManager.default
        guard let folders = try? fileManager.contentsOfDirectory(at: sessionDir.deletingLastPathComponent(), includingPropertiesForKeys: nil)
        else
        {
            print("iOSFileLogger: No files found for clean up")
            return
        }

        let dateRegex = #"^\d{4}-\d{2}-\d{2}$"#
        let dayFolders = folders.filter
        {
            $0.lastPathComponent.range(of: dateRegex, options: .regularExpression) != nil            
        }
        .sorted
        {
            $0.lastPathComponent > $1.lastPathComponent
        }

        guard dayFolders.count > 7
        else
        {
            return
        }

        for folder in dayFolders.dropLast(7)
        {
            try? fileManager.removeItem(at: folder)
        }
    }
}





