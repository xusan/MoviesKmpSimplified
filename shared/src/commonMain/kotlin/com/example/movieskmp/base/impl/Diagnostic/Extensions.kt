package com.base.impl.Diagnostic

fun <T> List<T>.ToDebugString(): String
{
    val sb = StringBuilder("List[${this.size}] ")

    for (item in this)
    {
        sb.append("$item, ")
    }

    return sb.toString()
}