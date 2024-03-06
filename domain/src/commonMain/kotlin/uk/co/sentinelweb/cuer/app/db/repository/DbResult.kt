package uk.co.sentinelweb.cuer.app.db.repository

sealed class DbResult<R> constructor(
    val isSuccessful: Boolean,
    val data: R? = null
) {
    open class Data<R>(data: R?) : DbResult<R>(true, data = data) {
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

    class Error<R>(
        val t: Throwable,
        val msg: String? = null,
        val code: String? = null
    ) : DbResult<R>(false)


    class Composite<R>(isSuccessful: Boolean, data: R?) : DbResult<R>(isSuccessful, data = data)
}