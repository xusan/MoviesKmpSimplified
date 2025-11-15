package com.base.abstractions.Messaging

interface IMessageEvent
{
    fun Subscribe(handler: (Any?)-> Unit)
    fun Unsubscribe(handler: (Any?)-> Unit)
    fun Publish(args: Any?)
}