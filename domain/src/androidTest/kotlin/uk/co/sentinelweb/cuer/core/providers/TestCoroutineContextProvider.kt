package uk.co.sentinelweb.cuer.core.providers

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher

@ExperimentalCoroutinesApi
class TestCoroutineContextProvider constructor(
    d: TestDispatcher,
) : CoroutineContextProvider(d, d, d, d)