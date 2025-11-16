import Foundation


@inlinable
@discardableResult
func synchronized<T>(_ nsLock: NSLock, _ block: () throws -> T) rethrows -> T
{
    nsLock.lock()
    defer { nsLock.unlock() }
    return try block()
}

func GenerateIndexPathRange(section: Int, start: Int, count: Int) -> [IndexPath]
{
   var result: [IndexPath] = []

   for n in 0..<count
   {
       let indexPath = IndexPath(row: start + n, section: section)
       result.append(indexPath)
   }

   return result
}

 func GenerateLoopedIndexPathRange(section: Int,
                                            sectionCount: Int,
                                            iterations: Int,
                                            start: Int,
                                            count: Int) -> [IndexPath]
{
   var result: [IndexPath] = []
   let step = sectionCount / iterations

   for r in 0..<iterations
   {
       for n in 0..<count
       {
           let index = start + (r * step) + n
           let indexPath = IndexPath(row: index, section: section)
           result.append(indexPath)
       }
   }

   return result
}
