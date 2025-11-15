package com.base.abstractions.Messaging

import kotlin.reflect.KClass
interface IMessagesCenter
{
    fun <TEvent : IMessageEvent> GetOrCreateEvent(eventClass: KClass<TEvent>, factory: () -> TEvent): TEvent
}

//Generic version of GetOrCreateEvent()
inline fun <reified TEvent : IMessageEvent> IMessagesCenter.GetEvent(noinline factory: () -> TEvent): TEvent {
    return GetOrCreateEvent(TEvent::class, factory)
}

