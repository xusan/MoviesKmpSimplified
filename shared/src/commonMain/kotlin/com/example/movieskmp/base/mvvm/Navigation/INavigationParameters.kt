package com.base.mvvm.Navigation

interface INavigationParameters
{
    /// Determines whether the <see cref="IParameters"/> contains the specified <paramref name="key"/>.
    /// <param name="key">The key to search the parameters for existence.</param>
    /// <returns>true if the <see cref="IParameters"/> contains a parameter with the specified key; otherwise, false.</returns>
    fun ContainsKey(key: String): Boolean;


    //Gets the parameter associated with the specified <paramref name="key"/>.
    // <typeparam name="T">The type of the parameter to get.</typeparam>
    // <param name="key">The key of the parameter to find.</param>
    // <returns>A matching value of <typeparamref name="T"/> if it exists.</returns>
    fun <T> GetValue(key: String) : T?;

    fun Count() : Int
}