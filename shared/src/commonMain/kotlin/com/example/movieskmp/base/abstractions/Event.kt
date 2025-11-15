package com.base.abstractions

import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

// Generic Event class to mimic C#-style events in Kotlin
// T is the type of data that the event will pass to its subscribers
@OptIn(ExperimentalObjCName::class)
@ObjCName(name = "BaseEvent", exact = true)
open class BaseEvent
{

    private val handlers = mutableListOf<() -> Unit>()

    operator fun plusAssign(handler: () -> Unit)
    {
        handlers.add(handler)
    }

    operator fun minusAssign(handler: () -> Unit)
    {
        handlers.remove(handler)
    }

    //swift-friendly version of plusAssign
    fun AddListener(listener: () -> Unit)
    {
        handlers.add(listener)
    }

    //swift-friendly version of minusAssign
    fun RemoveListener(listener: () -> Unit)
    {
        handlers.remove(listener)
    }

    open fun Invoke()
    {
        val handlersCopy = handlers.toList()
        handlersCopy.forEach { it() }
    }
}

@OptIn(ExperimentalObjCName::class)
@ObjCName(name = "Event", exact = true)
class Event<T> : BaseEvent()
{

    // List of functions (handlers) that will be called when the event is triggered
    private val handlers = mutableListOf<(T) -> Unit>()

    //Adds a new subscriber (handler) to the event.
    //Usage: event += { value -> println(value) }
    operator fun plusAssign(handler: (T) -> Unit)
    {
        handlers.add(handler)
    }


    //Removes a subscriber (handler) from the event.
    // Usage: event -= handlerReference
    operator fun minusAssign(handler: (T) -> Unit)
    {
        handlers.remove(handler)
    }

    //swift-friendly version of plusAssign
    fun AddListener(listener: (T) -> Unit)
    {
        handlers.add(listener)
    }

    //swift-friendly version of minusAssign
    fun RemoveListener(listener: (T) -> Unit)
    {
        handlers.remove(listener)
    }

    // Invokes the event, calling all subscribed handlers with the provided value.
    // Usage: event.Invoke(value)
    fun Invoke(value: T)
    {
        // Call each handler with the event value
        handlers.forEach { it(value) }
    }
}

//USAGE
//interface ErrorTracking
//{
//    val onError: Event<String>
//}
//
//class ErrorTrackerImpl : ErrorTracking
//{
//    override val onError = Event<String>()
//
//    fun simulateError(msg: String)
//    {
//        onError.invoke(msg)
//    }
//}
//
//class ErrorTrackerImpl : ErrorTracking
//{
//    override val onError = Event<String>()
//
//    fun simulateError(msg: String)
//    {
//        onError.invoke(msg)
//    }
//}
//
//fun main()
//{
//    val tracker: ErrorTracking = ErrorTrackerImpl()
//    tracker.onError += { println("Error: $it") }
//}
