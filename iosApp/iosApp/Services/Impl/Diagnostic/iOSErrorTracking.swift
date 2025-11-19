import SharedAppCore
import Sentry
import Dispatch

typealias Event = SharedAppCore.Event
typealias KotlinThrowable = SharedAppCore.KotlinThrowable
typealias IErrorTrackingService = SharedAppCore.IErrorTrackingService
typealias SentrySDK = Sentry.SentrySDK

class iOSErrorTrackingService : IErrorTrackingService
{
    var OnServiceError = Event<KotlinThrowable>()
    
    let TAG = "iOSErrorTrackingService: "
    
    func Initialize()
    {
        SentrySDK.start { options in
            
            options.dsn = "https://a368f5307caf72ccb720254591677b05@o4507288977080320.ingest.de.sentry.io/4510340982898768"
            options.debug = false
            options.beforeSend =
            { event in
                
                guard let exception = event.exceptions?.first else {
                    return event
                }
                //We need to convert Kotlin Throwable/Exception to Swift Error to correctly show at Sentry portal
                // Extract and clean from localized description
                let desc = exception.value
                if desc.contains("KotlinWrappedError(") {
                    // ─── Extract typeName value ───────────────────────────────
                    var cleanType = "KotlinException"
                    if let typeRange = desc.range(of: #"typeName: \"([^\"]+)\""#,
                                                  options: .regularExpression),
                       let match = try? NSRegularExpression(pattern: #"typeName: \"([^\"]+)\""#)
                        .firstMatch(in: desc, range: NSRange(typeRange, in: desc)) {
                        if let r = Range(match.range(at: 1), in: desc) {
                            cleanType = String(desc[r])
                        }
                    }
                    
                    // ─── Extract message value ────────────────────────────────
                    var cleanMessage = ""
                    if let match = try? NSRegularExpression(pattern: #"message:\s*Optional\(\"([^\"]+)\""#)
                        .firstMatch(in: desc, range: NSRange(location: 0, length: desc.utf16.count)),
                       let range = Range(match.range(at: 1), in: desc) {
                        cleanMessage = String(desc[range])
                    }
                    
                    //Get stack traces
                    var stackTraceLines: [String] = []
                    // Extract stackTrace contents (inside square brackets)
                    if let match = try? NSRegularExpression(pattern: #"stackTrace:\s*\[([^\]]+)\]"#)
                        .firstMatch(in: desc, range: NSRange(location: 0, length: desc.utf16.count)),
                       let range = Range(match.range(at: 1), in: desc) {
                        let rawStack = String(desc[range])
                        // Split by comma, clean whitespace and quotes
                        stackTraceLines = rawStack
                            .components(separatedBy: "\",")
                            .map { $0.replacingOccurrences(of: "\"", with: "").trimmingCharacters(in: .whitespacesAndNewlines) }
                    }
                    
                    // Build final formatted message
                    var formatted = "\(cleanType): \(cleanMessage)"
                    if !stackTraceLines.isEmpty {
                        formatted += "\n" + stackTraceLines.joined(separator: "\n")
                    }
                    // Update the event
                    exception.type = cleanType
                    exception.value = cleanMessage
                    event.message = SentryMessage(formatted: formatted)
                    event.exceptions?[0] = exception
                    
                    print("\(self.TAG)🪶 Cleaned Sentry event -> \(cleanType): \(cleanMessage)")
                }
                
                return event
            }
        }
    }
    
    func SetupAttachment()
    {
        let loggingService = try! KoinResolver().GetLoggingService()
        let currentLogPath = loggingService.GetCurrentLogFileName()
        if FileManager.default.fileExists(atPath: currentLogPath)
        {
            let fileAttachment = Attachment(path: currentLogPath)
            // Global Scope
            SentrySDK.configureScope { scope in
                scope.addAttachment(fileAttachment)
            }
        }
        else
        {
            loggingService.LogWarning(message: "\(TAG)Failed to attach log because it does not exist at \(currentLogPath)")
        }
    }
    
    func TrackError(ex: KotlinThrowable, attachment: KotlinByteArray?, additionalData: [String : String]?)
    {
        let error = ex.toReadableError()
        let id = SentrySDK.capture(error: error)
        //loggingService.LogWarning(message: "\(TAG)TrackError() send and returned id: \(id)")
        print("iOSErrorTrackingService.TrackError() send and returned id: \(id)")
    }
    
    
    
    
//    @discardableResult
//    func awaitSync<T>(_ operation: @escaping () async throws -> T) throws -> T
//    {
//        let semaphore = DispatchSemaphore(value: 0)
//        var result: Result<T, Error>!
//
//        Task {
//            do {
//                let value = try await operation()
//                result = .success(value)
//            } catch {
//                result = .failure(error)
//            }
//            semaphore.signal()
//        }
//
//        semaphore.wait()
//        return try result.get()
//    }
    
    
    
}



