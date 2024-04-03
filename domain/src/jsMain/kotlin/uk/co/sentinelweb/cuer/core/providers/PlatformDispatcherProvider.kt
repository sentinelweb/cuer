package uk.co.sentinelweb.cuer.core.providers

import kotlinx.coroutines.CoroutineDispatcher

actual object PlatformDispatcherProvider : DispatcherProvider {
    override val Main: CoroutineDispatcher
        get() = TODO("Not yet implemented")
    override val IO: CoroutineDispatcher
        get() = TODO("Not yet implemented")
    override val Computation: CoroutineDispatcher
        get() = TODO("Not yet implemented")
}