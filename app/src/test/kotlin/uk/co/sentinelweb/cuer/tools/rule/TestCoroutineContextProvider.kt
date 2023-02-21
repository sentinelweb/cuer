package uk.co.sentinelweb.cuer.tools.rule

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider

@ExperimentalCoroutinesApi
class TestCoroutineContextProvider constructor(
    d: CoroutineDispatcher,
) : CoroutineContextProvider(d, d, d, d)