package uk.co.sentinelweb.cuer.app.db.repository

sealed class RepoResult<R> constructor(
    val isSuccessful: Boolean,
    val data: R? = null
) {
    open class Data<R>(data: R?) : RepoResult<R>(true, data = data) {
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
    ) : RepoResult<R>(false)


    class Composite<R>(isSuccessful: Boolean, data: R?) : RepoResult<R>(isSuccessful, data = data)
}