package uk.co.sentinelweb.cuer.app.ui.common.dialog

open class DialogModel constructor(
    open val type: Type,
    open val title: String
) {
    enum class Type {
        PLAYLIST, PLAYLIST_ADD, CONFIRM, SELECT_ROUTE
    }
}