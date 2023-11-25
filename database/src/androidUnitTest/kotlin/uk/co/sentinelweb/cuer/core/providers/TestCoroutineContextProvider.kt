package uk.co.sentinelweb.cuer.core.providers

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class TestCoroutineContextProvider constructor(
    d: CoroutineDispatcher,
) : CoroutineContextProvider(d, d, d, d)