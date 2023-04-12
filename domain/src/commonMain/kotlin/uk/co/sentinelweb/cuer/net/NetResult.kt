package uk.co.sentinelweb.cuer.net


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
        val t: Throwable?,
        val msg: String? = t?.message.toString(),
        val code: String? = null
    ) : NetResult<R>(false)

    class NetworkError<R>(
        t: Exception,
        msg: String? = t.message.toString(),
        code: String? = null
    ) : Error<R>(t, msg, code)

    class HttpError<R>(
        t: Exception,
        msg: String = t.message.toString(),
        code: String
    ) : Error<R>(t, msg, code)

    class NotConnectedError<R>(
    ) : Error<R>(null, "Not connected", "NOT_CONNECTED")

}