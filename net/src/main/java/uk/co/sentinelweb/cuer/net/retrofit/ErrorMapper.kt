package uk.co.sentinelweb.cuer.net.retrofit

import retrofit2.HttpException
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.net.NetResult
import java.io.IOException

internal class ErrorMapper(
    internal val log: LogWrapper
) {

    fun <R> map(t: Throwable, msg: String): NetResult.Error<R> = when (t) {
        is IOException -> {
            log.e(msg, t)
            NetResult.NetworkError<R>(t)
        }
        is HttpException -> {
            log.e(msg, t)
            NetResult.HttpError<R>(t)
        }
        else -> {
            log.e(msg, t)
            NetResult.Error<R>(t)
        }
    }
}