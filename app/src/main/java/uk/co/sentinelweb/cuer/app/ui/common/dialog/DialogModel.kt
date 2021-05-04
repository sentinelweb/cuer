package uk.co.sentinelweb.cuer.app.ui.common.dialog

import androidx.annotation.StringRes

open class DialogModel constructor(
    open val type: Type,
    @StringRes open val title: Int
) {
    enum class Type {
        PLAYLIST, PLAYLIST_ADD, PLAYLIST_FULL, CONFIRM, SELECT_ROUTE, SKIP_TIME, DATE_RANGE_PICKER, DATE_PICKER, DISMISS, ENUM_VALUES
    }

    class DismissDialogModel() : DialogModel(Type.DISMISS, 0)
}