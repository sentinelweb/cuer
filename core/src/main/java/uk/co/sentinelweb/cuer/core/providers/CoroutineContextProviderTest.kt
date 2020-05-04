package uk.co.sentinelweb.cuer.core.providers

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

class CoroutineContextProviderTest() : CoroutineContextProvider() {
    override val Main: CoroutineContext = Dispatchers.Unconfined
    override val IO: CoroutineContext = Dispatchers.Unconfined
    override val MainScope: CoroutineScope = CoroutineScope(Dispatchers.Unconfined)
    override val IOScope: CoroutineScope = CoroutineScope(Dispatchers.Unconfined)

}