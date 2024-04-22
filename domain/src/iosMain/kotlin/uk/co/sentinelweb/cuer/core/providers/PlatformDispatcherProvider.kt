package uk.co.sentinelweb.cuer.core.providers

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.newFixedThreadPoolContext

actual object PlatformDispatcherProvider : DispatcherProvider {
    override val Main: CoroutineDispatcher
        get() = Dispatchers.Main
    override val IO: CoroutineDispatcher
        get() = newFixedThreadPoolContext((3), "IO Dispatcher")
    override val Computation: CoroutineDispatcher
        get() = newFixedThreadPoolContext((3), "Computation Dispatcher")
}