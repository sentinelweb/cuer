package uk.co.sentinelweb.cuer.app.ui.common.chip

class ChipModel constructor(
    val type: Type,
    val text: String? = null,
    val value: String? = null
) {
    enum class Type {
        PLAYLIST, PLAYLIST_SELECT
    }
}