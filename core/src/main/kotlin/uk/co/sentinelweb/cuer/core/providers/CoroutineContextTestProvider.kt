package uk.co.sentinelweb.cuer.core.providers

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class CoroutineContextTestProvider(
    d: CoroutineDispatcher = Dispatchers.Unconfined
) : CoroutineContextProvider() {
    override val Main: CoroutineDispatcher by lazy { d }
    override val IO: CoroutineDispatcher by lazy { d }
    override val Default: CoroutineDispatcher by lazy { d }
}