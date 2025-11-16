import SharedAppCore
import Foundation
import ZIPFoundation

class iOSZipService: IZipService
{
    func CreateFromDirectoryAsync(fileDir: String, zipPath: String) async throws
    {
        let fileManager = FileManager.default
        let sourceURL = URL(fileURLWithPath: fileDir)
        let destinationURL = URL(fileURLWithPath: zipPath)
        
        // Remove existing file if overwriting
        if fileManager.fileExists(atPath: destinationURL.path)
        {
            try fileManager.removeItem(at: destinationURL)
        }
        
        
        // Compress directory into ZIP
        try fileManager.zipItem(at: sourceURL, to: destinationURL)
    }
    
    func CreateFromFileAsync(filePath: String, zipPath: String) async throws
    {
        let fileManager = FileManager.default
        let sourceURL = URL(fileURLWithPath: filePath)
        let destinationURL = URL(fileURLWithPath: zipPath)
        
        if fileManager.fileExists(atPath: destinationURL.path)
        {
            try fileManager.removeItem(at: destinationURL)
        }
        
        // Wrap single file in a temporary directory so ZIPFoundation can handle it
        let tempDir = fileManager.temporaryDirectory.appendingPathComponent(UUID().uuidString)
        try fileManager.createDirectory(at: tempDir, withIntermediateDirectories: true)
        
        let tempFile = tempDir.appendingPathComponent(sourceURL.lastPathComponent)
        try fileManager.copyItem(at: sourceURL, to: tempFile)
        
        try fileManager.zipItem(at: tempDir, to: destinationURL)
        
        // Clean up temporary directory
        try? fileManager.removeItem(at: tempDir)
    }
    
    func ExtractToDirectoryAsync(filePath zipPath: String, dir dirPath: String, overwrite: Bool) async throws
    {
        let fileManager = FileManager.default
        let zipURL = URL(fileURLWithPath: zipPath)
        let destinationURL = URL(fileURLWithPath: dirPath)
        
        if overwrite && fileManager.fileExists(atPath: destinationURL.path)
        {
            try fileManager.removeItem(at: destinationURL)
        }
        
        try fileManager.createDirectory(at: destinationURL, withIntermediateDirectories: true)
        try fileManager.unzipItem(at: zipURL, to: destinationURL)
    }
}
