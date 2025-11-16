//
//  StringExtensions.swift
//  iosApp
//
//  Created by xusan on 11/11/25.
//

import Foundation

extension String
{
    func propertyName() -> String
    {
       components(separatedBy: ".").last ?? self
    }
    
    func isLocalFilePath() -> Bool
    {
        return FileManager.default.fileExists(atPath: self)
    }
    
    func isRemoteUrl() -> Bool
    {
        guard let url = URL(string: self) else { return false }
        return url.scheme == "http" || url.scheme == "https"
    }
}
