package uk.co.sentinelweb.cuer.net

import retrofit2.HttpException
import java.io.IOException

sealed class NetResult<R> constructor(
    val isSuccessful: Boolean,
    val data: R? = null
) {
    open class Data<R>(data: R?) : NetResult<R>(true, data = data) {
        class Empty<R>(data: R? = null) : Data<R>(data)

        companion object {
            fun <R> dataOrEmpty(list: R): Data<R> =
                if (list is List<*> && list.isEmpty()) {
                    Empty(list)
                } else if (list == null) {
                    Empty()
                } else {
                    Data(list)
                }
        }
    }

    open class Error<R>(
        val t: Throwable,
        val msg: String? = null,
        val code: String? = null
    ) : NetResult<R>(false)

    class NetworkError<R>(
        t: IOException,
        msg: String? = null,
        code: String? = null
    ) : Error<R>(t, msg, code)

    class HttpError<R>(
        t: HttpException,
        msg: String = t.message().toString(),
        code: String = t.code().toString()
    ) : Error<R>(t, msg, code)
}