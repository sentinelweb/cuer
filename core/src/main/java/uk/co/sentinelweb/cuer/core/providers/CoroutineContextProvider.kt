package uk.co.sentinelweb.cuer.core.providers

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

// should this be single or factory .. not sure
// todo look at viewmodel scope
open class CoroutineContextProvider {
    open val Main: CoroutineContext = Dispatchers.Main
    open val IO: CoroutineContext = Dispatchers.IO
    open val MainScope: CoroutineScope = CoroutineScope(Dispatchers.Main)
    open val IOScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
}