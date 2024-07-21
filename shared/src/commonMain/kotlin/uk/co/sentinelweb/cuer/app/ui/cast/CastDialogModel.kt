package uk.co.sentinelweb.cuer.app.ui.cast

data class CastDialogModel(
    val connectionStatus: String,
    val cuerCastStatus: CuerCastStatus,
    val chromeCastStatus: ChromeCastStatus,
    val floatingStatus: Boolean,
) {
    data class CuerCastStatus(
        val isConnected: Boolean = false,
        val connectedHost: String? = null,
        val isPlaying: Boolean = false
    )

    data class ChromeCastStatus(
        val isConnected: Boolean = false,
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