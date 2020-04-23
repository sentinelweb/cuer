package uk.co.sentinelweb.cuer.core.providers

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

// should this be single or factory .. not sure
// todo look at viewmodel scope
class CoroutineContextProvider() {
    val Main: CoroutineContext = Dispatchers.Main
    val IO: CoroutineContext = Dispatchers.IO
    val MainScope: CoroutineScope = CoroutineScope(Dispatchers.Main)
    val IOScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
}