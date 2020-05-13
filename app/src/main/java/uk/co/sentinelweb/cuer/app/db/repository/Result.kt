package uk.co.sentinelweb.cuer.app.db.repository

sealed class Result<R> constructor(
    val isSuccessful: Boolean,
    val data: R? = null
) {
    open class Data<R>(data: R?) : Result<R>(true, data = data) {
        class Empty<R>(data: R? = null) : Data<R>(data)

        companion object {
            fun <R> dataOrEmpty(list: R): Data<R> =
                if (list is List<*> && list.isEmpty()) {
                    Empty(list)
                } else {
                    Data(list)
                }
        }
    }

    class Error<R>(
        val t: Throwable,
        val msg: String? = null,
        val code: String? = null
    ) : Result<R>(false)
}