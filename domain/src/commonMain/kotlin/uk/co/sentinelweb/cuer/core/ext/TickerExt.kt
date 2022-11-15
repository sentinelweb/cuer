package uk.co.sentinelweb.cuer.core.ext

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow

fun tickerFlow(periodMs: Long, initialDelayMs: Long = 0L) = flow {
    delay(initialDelayMs)
    while (true) {
        emit(Unit)
        delay(periodMs)
    }
}