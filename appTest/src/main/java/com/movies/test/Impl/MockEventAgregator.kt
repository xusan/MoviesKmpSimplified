package com.movies.test.Impl

import com.base.abstractions.Messaging.IMessageEvent
import com.base.abstractions.Messaging.IMessagesCenter
import kotlin.reflect.KClass

class MockEventAgregator : IMessagesCenter
{
    override fun <TEvent : IMessageEvent> GetOrCreateEvent(eventClass: KClass<TEvent>, factory: () -> TEvent): TEvent
    {
        return factory();
    }

}