package uk.co.sentinelweb.cuer.net.client

import io.ktor.utils.io.errors.*
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.net.NetResult

internal class ErrorMapper(
    internal val log: LogWrapper
) {

    fun <R> map(t: Throwable, msg: String): NetResult.Error<R> = when (t) {
        is IOException -> {
            log.e(msg, t)
            NetResult.NetworkError<R>(t)
        }

        is RequestFailureException -> {
            log.e(msg, t)
            NetResult.HttpError<R>(t, code = t.code.toString(), msg = t.description)
        }

        else -> {
            log.e(msg, t)
            NetResult.Error<R>(t, msg = t.message)
        }
    }

    fun <R> notConnected() = NetResult.NotConnectedError<R>()
}