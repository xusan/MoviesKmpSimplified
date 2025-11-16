import SharedAppCore
import Foundation

class iOSPreferencesImplementation: IPreferences
{    
    private let lock = NSLock()
    
    func ContainsKey(key: String, sharedName: String?) -> Bool
    {
        return synchronized(lock)
        {
            let userDefaults = GetUserDefaults(sharedName)
            return userDefaults.object(forKey: key) != nil
        }
    }
    
    func Remove(key: String, sharedName: String?)
    {
        synchronized(lock)
        {
            let userDefaults = GetUserDefaults(sharedName)
            if userDefaults.object(forKey: key) != nil
            {
                userDefaults.removeObject(forKey: key)
            }
        }
    }
    
    func Clear(sharedName: String?)
    {
        synchronized(lock)
        {
            let userDefaults = GetUserDefaults(sharedName)
            if let dict = userDefaults.dictionaryRepresentation() as NSDictionary?
            {
                for (key, _) in dict
                {
                    if let nsKey = key as? NSString
                    {
                        userDefaults.removeObject(forKey: nsKey as String)
                    }
                }
            }
        }
    }
    
    func Set(key: String, value: Any?, sharedName: String?)
    {
        synchronized(lock)
        {
            let userDefaults = GetUserDefaults(sharedName)
            
            if let v = value
            {
                switch v
                {
                case let s as String:
                    userDefaults.set(s, forKey: key)
                case let i as Int:
                    userDefaults.set(i, forKey: key)
                case let b as Bool:
                    userDefaults.set(b, forKey: key)
                case let l as Int64:
                    userDefaults.set(String(l), forKey: key)
                case let d as Double:
                    userDefaults.set(d, forKey: key)
                case let f as Float:
                    userDefaults.set(f, forKey: key)                
                default:
                    userDefaults.set(String(describing: v), forKey: key)
                }
            }
            else
            {
                if userDefaults.object(forKey: key) != nil
                {
                    userDefaults.removeObject(forKey: key)
                }
            }
        }
    }
    
    func Get(key: String, defaultValue: Any?, sharedName: String?) -> Any?
    {
        var value: Any? = nil
        
        synchronized(lock)
        {
            let userDefaults = GetUserDefaults(sharedName)
            let stored = userDefaults.object(forKey: key)
            
            guard stored != nil else
            {
                value = defaultValue
                return
            }
            
            switch defaultValue
            {
            case is Int:
                value = userDefaults.integer(forKey: key)
            case is Bool:
                value = userDefaults.bool(forKey: key)
            case is Int64:
                if let str = userDefaults.string(forKey: key)
                {
                    value = Int64(str)
                }
            case is Double:
                value = userDefaults.double(forKey: key)
            case is Float:
                value = userDefaults.float(forKey: key)
            case is String:
                value = userDefaults.string(forKey: key)
            default:
                value = userDefaults.string(forKey: key)
            }
        }
        
        return value
    }
    
    private func GetUserDefaults(_ sharedName: String?) -> UserDefaults
    {
        if let name = sharedName, !name.isEmpty
        {
            return UserDefaults(suiteName: name) ?? UserDefaults.standard
        }
        else
        {
            return UserDefaults.standard
        }
    }
}

extension IPreferences
{
    
    func Get<T>(_ key: String, default defaultValue: T, sharedName: String? = nil) -> T {
        // Call the generated Kotlin method
        let result = Get(key: key, defaultValue: defaultValue as AnyObject, sharedName: sharedName)
        
        // Attempt to cast back to expected type
        return (result as? T) ?? defaultValue
    }
}

