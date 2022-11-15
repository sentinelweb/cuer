package uk.co.sentinelweb.cuer.core.providers

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.newFixedThreadPoolContext

actual object PlatformDispatcherProvider : DispatcherProvider {
    // todo review this - maybe just inject CoroutineDispatcher with enums?
    //private val computationDispatcher
    override val IO: CoroutineDispatcher
        get() = newFixedThreadPoolContext((3), "IO Dispatcher")
    override val Computation: CoroutineDispatcher
        get() = newFixedThreadPoolContext((3), "Computation Dispatcher")
}