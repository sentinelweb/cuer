package uk.co.sentinelweb.cuer.app.ui.common.dialog

data class SelectDialogModel constructor(
    val type: Type,
    val title: String,
    val items: List<Item>
) {
    data class Item constructor(
        val name: String,
        val selected: Boolean = false
    )

    enum class Type {
        PLAYLIST, PLAYLIST_ITEM, MEDIA
    }
}