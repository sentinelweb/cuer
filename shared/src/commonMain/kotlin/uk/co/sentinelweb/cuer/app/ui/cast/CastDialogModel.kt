package uk.co.sentinelweb.cuer.app.ui.cast

data class CastDialogModel(
    val connectionStatus: String,
    val cuerCastStatus: CuerCastStatus,
    val chromeCastStatus: ChromeCastStatus,
    val floatingStatus: Boolean,
) {
    data class CuerCastStatus(
        val connected: Boolean = false,
        val connectedHost: String? = null,
    )

    data class ChromeCastStatus(
        val connected: Boolean = false,
        val connectedHost: String? = null,
    )

    companion object {
        val blank = CastDialogModel(
            connectionStatus = "Not connected",
            cuerCastStatus = CuerCastStatus(),
            chromeCastStatus = ChromeCastStatus(),
            floatingStatus = false
        )
    }
}