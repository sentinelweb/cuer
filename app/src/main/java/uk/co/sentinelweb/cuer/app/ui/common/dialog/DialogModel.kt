package uk.co.sentinelweb.cuer.app.ui.common.dialog

import androidx.annotation.StringRes

open class DialogModel constructor(
    open val type: Type,
    @StringRes open val title: Int
) {
    enum class Type {
        PLAYLIST, PLAYLIST_ADD, CONFIRM, SELECT_ROUTE
    }
}