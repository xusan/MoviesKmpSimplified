import SharedAppCore
import OSLog

class iOSConsoleOutput: IPlatformOutput
{
    let logger: Logger;
    init()
    {
        logger = Logger(subsystem: "", category: "AppLogger")
    }
    
    func Error(message: String)
    {
        logger.fault("\(message)")
    }
    
    func Info(message: String)
    {
        logger.info("\(message)")
    }
    
    func Warn(message: String)
    {
        logger.warning("\(message)")
    }
    
    
}

