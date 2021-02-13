package uk.co.sentinelweb.cuer.core.providers

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class CoroutineContextTestProvider(
    d: CoroutineDispatcher = Dispatchers.Unconfined
) : CoroutineContextProvider(d, d, d, d)