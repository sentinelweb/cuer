package uk.co.sentinelweb.cuer.core.providers

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

actual object PlatformDispatcherProvider : DispatcherProvider {
    // todo review this - maybe just inject CoroutineDispatcher with enums?
    //private val computationDispatcher
    override val IO: CoroutineDispatcher
        get() = Dispatchers.IO
    override val Computation: CoroutineDispatcher
        get() = Executors.newFixedThreadPool(2).asCoroutineDispatcher()
}