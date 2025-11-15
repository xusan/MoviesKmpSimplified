package com.base.mvvm.Actuals

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Runnable
import kotlin.coroutines.CoroutineContext
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

// Use a custom dispatcher for Main on iOS
actual object SharedDispatchers {
    actual val Main: CoroutineDispatcher = NsQueueMainDispatcher
    actual val Default: CoroutineDispatcher = Dispatchers.Default
}

// Implementation of iOS main thread dispatcher
private object NsQueueMainDispatcher : CoroutineDispatcher() {
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        dispatch_async(dispatch_get_main_queue()) {
            block.run()
        }
    }
}