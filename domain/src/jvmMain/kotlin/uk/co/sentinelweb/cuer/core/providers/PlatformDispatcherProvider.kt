package uk.co.sentinelweb.cuer.core.providers

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors
import javax.swing.SwingUtilities
import kotlin.coroutines.CoroutineContext

actual object PlatformDispatcherProvider : DispatcherProvider {
    override val Main: CoroutineDispatcher
        get() = object : CoroutineDispatcher() {
            override fun dispatch(context: CoroutineContext, block: Runnable) {
                SwingUtilities.invokeLater(block)
            }
        }
    override val IO: CoroutineDispatcher
        get() = Dispatchers.IO
    override val Computation: CoroutineDispatcher
        get() = Executors.newFixedThreadPool(2).asCoroutineDispatcher()
}