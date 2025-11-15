package com.base.mvvm.Helpers

import com.base.abstractions.Event

class ObservableCollection<T>(private val itemsInternal: MutableList<T> = mutableListOf())
{
    // Expose read-only list to outside callers
    val Items: List<T>
        get() = itemsInternal

    val CollectionChanged = Event<Change>()

    fun Add(element: T)
    {
        itemsInternal.add(element)
        val index = itemsInternal.lastIndex
        CollectionChanged.Invoke(Change.Added(element as Any, index))
    }

    fun Add(index: Int, element: T)
    {
        itemsInternal.add(index, element)
        CollectionChanged.Invoke(Change.Added(element as Any, index))
    }

    fun Remove(element: T)
    {
        val index = itemsInternal.indexOf(element)
        if (index != -1)
        {
            itemsInternal.removeAt(index)
            CollectionChanged.Invoke(Change.Removed(element as Any, index))
        }
    }

    fun RemoveAt(index: Int)
    {
        if (index in itemsInternal.indices)
        {
            val removed = itemsInternal.removeAt(index)
            CollectionChanged.Invoke(Change.Removed(removed as Any, index))
        }
    }

    fun Clear()
    {
        if (itemsInternal.isNotEmpty())
        {
            itemsInternal.clear()
            CollectionChanged.Invoke(Change.Cleared)
        }
    }

    fun Replace(index: Int, newItem: T)
    {
        if (index in itemsInternal.indices)
        {
            val oldItem = itemsInternal[index]
            itemsInternal[index] = newItem
            CollectionChanged.Invoke(Change.Replaced(oldItem as Any, newItem as Any, index))
        }
    }

    fun Count(): Int
    {
        return itemsInternal.size
    }

    operator fun get(index: Int): T
    {
        return itemsInternal[index]
    }

    sealed class Change
    {
        data class Added(val item: Any, val index: Int) : Change()
        data class Removed(val item: Any, val index: Int) : Change()
        data class Replaced(val oldItem: Any, val newItem: Any, val index: Int) : Change()
        data object Cleared : Change()
    }
}


//class ObservableCollection<T>(
//    private val items: MutableList<T> = mutableListOf()
//) : MutableList<T> by items {
//
//    val CollectionChanged = Event<Change>();
//
//    override fun add(element: T): Boolean
//    {
//        val result = items.add(element)
//        val index = items.indexOf(element)
//        if (result) CollectionChanged.Invoke(Change.Added(element as Any, index))
//        return result
//    }
//
//    override fun add(index: Int, element: T)
//    {
//        items.add(index, element)
//        CollectionChanged.Invoke(Change.Added(element as Any, index))
//    }
//
//    override fun remove(element: T): Boolean {
//        val index = items.indexOf(element)
//        val result = items.remove(element)
//        if (result) CollectionChanged.Invoke(Change.Removed(element as Any, index))
//        return result
//    }
//
//    override fun clear() {
//        val oldItems = items.toList()
//        items.clear()
//        CollectionChanged.Invoke(Change.Cleared)
//    }
//
//    fun replace(index: Int, newItem: T) {
//        val oldItem = items[index]
//        items[index] = newItem
//        CollectionChanged.Invoke(Change.Replaced(oldItem as Any, newItem as Any, index))
//    }
//
//
//    sealed class Change {
//        data class Added(val item: Any, val index: Int) : Change()
//        data class Removed(val item: Any, val index: Int) : Change()
//        data class Replaced(val oldItem: Any, val newItem: Any, val index: Int) : Change()
//        data object Cleared : Change()
//    }
//}