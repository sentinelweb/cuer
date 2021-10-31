package uk.co.sentinelweb.cuer.core.providers

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher

@ExperimentalCoroutinesApi
class TestCoroutineContextProvider constructor(
    d: TestCoroutineDispatcher,
) : CoroutineContextProvider(d, d, d, d)