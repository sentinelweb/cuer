package uk.co.sentinelweb.cuer.core.providers

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

actual object PlatformDispatcherProvider : DispatcherProvider {
    override val IO: CoroutineDispatcher
        get() = Dispatchers.IO
    override val Computation: CoroutineDispatcher
        get() = Executors.newFixedThreadPool(2).asCoroutineDispatcher()
}