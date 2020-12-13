package uk.co.sentinelweb.cuer.app.ui.common.dialog

data class SelectDialogModel constructor(
    val type: Type,
    val multi: Boolean,
    val title: String,
    val items: List<Item> = listOf()
) {
    data class Item constructor(
        val name: String,
        val selected: Boolean = false,
        val selectable: Boolean = false
    )

    enum class Type {
        PLAYLIST, PLAYLIST_ADD, PLAYLIST_ITEM, MEDIA
    }
}