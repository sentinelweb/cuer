package uk.co.sentinelweb.cuer.app.ui.cast

data class CastDialogModel(
    val connectionSummary: String,
    val cuerCastStatus: CuerCastStatus,
    val chromeCastStatus: ChromeCastStatus,
    val floatingStatus: Boolean,
) {
    data class CuerCastStatus(
        val isConnected: Boolean = false,
        val connectedHost: String? = null,
        val isPlaying: Boolean = false,
        val volumePercent: String = "0%",
    )

    data class ChromeCastStatus(
        val isConnected: Boolean = false,
        val connectedHost: String? = null,
        val volumePercent: String = "0%",
    )

    companion object {
        val blank = CastDialogModel(
            connectionSummary = "Not connected",
            cuerCastStatus = CuerCastStatus(),
            chromeCastStatus = ChromeCastStatus(),
            floatingStatus = false,
        )
    }
}