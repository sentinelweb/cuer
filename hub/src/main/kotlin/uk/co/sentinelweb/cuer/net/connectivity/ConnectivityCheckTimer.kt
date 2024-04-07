package uk.co.sentinelweb.cuer.net.connectivity

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ConnectivityCheckTimer(private val periodMs: Long = 2000) {
    fun tick(): Flow<Unit> = flow {
        while (true) {
            delay(periodMs)
            emit(Unit)
        }
    }
}