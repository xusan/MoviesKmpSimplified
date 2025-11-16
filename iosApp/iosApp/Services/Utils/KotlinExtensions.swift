import Foundation
import SharedAppCore
typealias KotlinByteArray = SharedAppCore.KotlinByteArray

extension Data
{
    func toKotlinByteArray() -> KotlinByteArray
    {
        let byteArray = KotlinByteArray(size: Int32(self.count))
        for (i, byte) in self.enumerated()
        {
            byteArray.set(index: Int32(i), value: Int8(bitPattern: byte))
        }
        return byteArray
    }
}

extension KotlinByteArray
{
    func toData() -> Data
    {
        var bytes = [UInt8]()
        bytes.reserveCapacity(Int(size))
        
        for i in 0..<Int(size)
        {
            let value = self.get(index: Int32(i))
            bytes.append(UInt8(bitPattern: value))
        }
        
        return Data(bytes)
    }
}

extension ILoggingService
{
    public func LogError(error: Error, message: String = "")
    {
        self.LogError(ex: KotlinThrowable(message: error.localizedDescription), message: message, handled: true)
    }
}


struct KotlinWrappedError: Error, LocalizedError {
    let typeName: String
    let message: String?
    let stackTrace: [String]

    var errorDescription: String? {
        if let message = message {
            return "\(typeName): \(message)"
        } else {
            return typeName
        }
    }

    // Optional extra metadata for Sentry
    var failureReason: String? { message }
}

extension KotlinThrowable
{
    func toReadableError() -> KotlinWrappedError
    {
        let rawDescription = self.description()
        let (className, message): (String, String?)
        
        if let colonIndex = rawDescription.firstIndex(of: ":")
        {
            let namePart = rawDescription[..<colonIndex]
            let msgPart = rawDescription[rawDescription.index(after: colonIndex)...].trimmingCharacters(in: .whitespaces)
            className = namePart.components(separatedBy: ".").last ?? String(namePart)
            message = msgPart
        }
        else
        {
            className = String(describing: type(of: self))
            message = self.message
        }
        
        let traceArray = self.getStackTrace()
        let stack = (0..<Int(traceArray.size)).compactMap { i in
            (traceArray.get(index: Int32(i))).map { $0 as String }
        }
        
        return KotlinWrappedError(typeName: className, message: message, stackTrace: stack)
    }
    
    func makeNSError() -> NSError {
        let rawDescription = self.description()  // e.g. "java.lang.NumberFormatException: Test Error..."
        var typeName = "KotlinException"
        var message = self.message ?? ""

        // Extract from description if possible
        if let colon = rawDescription.firstIndex(of: ":") {
            let rawType = rawDescription[..<colon]
            typeName = rawType.components(separatedBy: ".").last ?? String(rawType)
            let rawMsg = rawDescription[rawDescription.index(after: colon)...]
            message = rawMsg.trimmingCharacters(in: .whitespaces)
        } else {
            // Fallback to Swift type name if description has no colon
            typeName = String(describing: type(of: self))
        }

        // Build NSError for Sentry
        let userInfo: [String: Any] = [
            NSLocalizedDescriptionKey: "\(typeName): \(message)",
            "Message": message,
            "ExceptionType": typeName
        ]
        return NSError(domain: typeName, code: 0, userInfo: userInfo)
    }
}
