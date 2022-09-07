package uk.co.sentinelweb.cuer.core.providers

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher

@ExperimentalCoroutinesApi
class TestCoroutineContextProvider constructor(
    d: CoroutineDispatcher,
) : CoroutineContextProvider(d, d, d, d)