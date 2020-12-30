package uk.co.sentinelweb.cuer.core.providers

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

class CoroutineContextTestProvider(
    d: CoroutineDispatcher = Dispatchers.Unconfined
) :
    CoroutineContextProvider() {
    override val Main: CoroutineContext by lazy { d }
    override val IO: CoroutineContext by lazy { d }
    override val MainScope: CoroutineScope by lazy { CoroutineScope(d) }
    override val IOScope: CoroutineScope by lazy { CoroutineScope(d) }
    override val Default: CoroutineContext by lazy { d }
    override val DefaultScope: CoroutineScope by lazy { CoroutineScope(d) }
}