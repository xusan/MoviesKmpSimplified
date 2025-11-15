package com.base.mvvm.Navigation

class NavigationParameters : INavigationParameters
{
    constructor()
    {

    }
    constructor(ket: String, value: Any?)
    {
        add(ket, value)
    }
    constructor(builder: NavigationParameters.() -> Unit = {})
    {
        builder()
    }

    private val entries = mutableListOf<Pair<String, Any?>>()

    /** Add or update a key-value pair */
    fun add(key: String, value: Any?)
    {
        val index = entries.indexOfFirst { it.first == key }
        if (index >= 0)
        {
            entries[index] = key to value
        }
        else
        {
            entries.add(key to value)
        }
    }

    /** Indexer-style syntax: parameters["key"] = value */
    operator fun set(key: String, value: Any?) = add(key, value)

    /** Get the value for a key */
    operator fun get(key: String): Any? = entries.firstOrNull { it.first == key }?.second

    /** Check if key exists */
    override fun ContainsKey(key: String): Boolean = entries.any { it.first == key }

    /** Generic getter */
    override fun <T> GetValue(key: String): T? = this[key] as? T
    override fun Count(): Int
    {
        return entries.count()
    }

    /** Optional: iterate over all entries */
    fun allEntries(): List<Pair<String, Any?>> = entries.toList()
}