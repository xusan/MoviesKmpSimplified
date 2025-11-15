package com.base.mvvm.Actuals

import kotlinx.coroutines.CoroutineDispatcher

expect object SharedDispatchers {
    val Main: CoroutineDispatcher
    val Default: CoroutineDispatcher
}