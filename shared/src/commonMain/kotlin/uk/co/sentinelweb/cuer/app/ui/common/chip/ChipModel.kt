package uk.co.sentinelweb.cuer.app.ui.common.chip

import uk.co.sentinelweb.cuer.domain.ImageDomain


data class ChipModel constructor(
    val type: Type,
    val text: String,
    val value: String? = null,
    val thumb: ImageDomain? = null,
    val deleteable: Boolean = true
) {
    enum class Type {
        PLAYLIST, PLAYLIST_SELECT
    }

    companion object {
        val PLAYLIST_SELECT_MODEL = ChipModel(Type.PLAYLIST_SELECT, "Playlists ...")
    }
}