package com.base.abstractions.Messaging

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlin.reflect.KClass

open class SubMessage : IMessageEvent
{
    private val handlers = mutableListOf<(Any?) -> Unit>()
    private val lock = SynchronizedObject()

    override fun Subscribe(handler: (Any?) -> Unit)
    {
        synchronized(lock)
        {
            if (!handlers.contains(handler))
                handlers.add(handler)
        }
    }

    override fun Unsubscribe(handler: (Any?) -> Unit)
    {
        synchronized(lock)
        {
            handlers.remove(handler)
        }
    }

    override fun Publish(args: Any?)
    {
        val snapshot: List<(Any?) -> Unit> = synchronized(lock) { handlers.toList() }
        snapshot.forEach { it(args) }
    }
}

class SimpleMessageCenter : IMessagesCenter
{
    private val events = mutableMapOf<KClass<*>, Any>()
    private val lock = SynchronizedObject()

    @Suppress("UNCHECKED_CAST")
    override fun <TEvent : IMessageEvent> GetOrCreateEvent(eventClass: KClass<TEvent>, factory: () -> TEvent): TEvent
    {
        synchronized(lock)
        {
            val existing = events[eventClass]
            if (existing != null)
                return existing as TEvent

            //Instead of trying to instantiate TEvent reflectively, let the caller tell you how to create it.
            //reflection (KClass.constructors), which is not available in commonMain because:
            //The constructors property (and most reflective features) are only implemented on the JVM runtime —
            // And not on Native (iOS) or JS.
            //let the caller tell you how to create it via factory() lambada.
            val newInstance = factory();
            events[eventClass] = newInstance;
            return newInstance;
        }
    }


    //*******USAGE*********
    //val event = center.GetEvent(SubMessage::class) { SubMessage() }
    //val event = center.GetEvent<SubMessage>() { SubMessage() }
}

