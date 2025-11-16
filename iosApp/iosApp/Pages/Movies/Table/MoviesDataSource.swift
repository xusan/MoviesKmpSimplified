import Foundation
import AsyncDisplayKit
import UIKit
import SharedAppCore

final class MoviesDataSource : NSObject, ASTableDataSource
{
    private var page: MoviesPage
    private var collection: ObservableCollection<MovieItemViewModel>?
    
    init(pageNode: MoviesPage)
    {
        self.page = pageNode
        super.init()
    }
    
    func onCollectionSet()
    {
        if let collection = collection
        {
            collection.CollectionChanged.RemoveListener(listener_: MoviesItems_CollectionChanged)
        }
        
        collection = page.vm.MovieItems
        collection?.CollectionChanged.AddListener(listener_: MoviesItems_CollectionChanged)
        
        page.tableNode.reloadData()
    }
    
    func numberOfSections(in tableNode: ASTableNode) -> Int
    {
        return 1
    }
    
    func tableNode(_ tableNode: ASTableNode, numberOfRowsInSection section: Int) -> Int
    {
        if let count = collection?.Count()
        {
            return Int(count)
        }
        
        return 0
    }
    
    func tableNode(_ tableNode: ASTableNode, nodeBlockForRowAt indexPath: IndexPath) -> ASCellNodeBlock
    {
        guard let model = collection?.Items[indexPath.row] as? MovieItemViewModel else
        {
            return { ASCellNode() }
        }
        
        return { CellMovie(model: model) }
    }
    
    private func MoviesItems_CollectionChanged(e: ObservableCollectionChange?)
    {
        switch e
        {
        case let added as ObservableCollectionChange.Added:
            let index = IndexPath(row: Int(added.index), section: 0)
            self.page.tableNode.insertRows(at: [index], with: .automatic)
            if added.index == 0
            {
                self.page.tableNode.scrollToRow(at: index, at: .top, animated: true)
            }
            
        case let removed as ObservableCollectionChange.Removed:
            let index = IndexPath(row: Int(removed.index), section: 0)
            self.page.tableNode.deleteRows(at: [index], with: .automatic)
            
        case let replaced as ObservableCollectionChange.Replaced:
            let index = IndexPath(row: Int(replaced.index), section: 0)
            self.page.tableNode.reloadRows(at: [index], with: .automatic)
            
        case _ as ObservableCollectionChange.Cleared:
            self.page.tableNode.reloadData()
            
        default:
            break
        }
    }
}

