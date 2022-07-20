package uk.co.sentinelweb.cuer.app.ui.common.dialog

open class DialogModel constructor(
    open val type: Type,
    open val title: Int// todo change to string
) {
    enum class Type {
        PLAYLIST, PLAYLIST_ADD, PLAYLIST_FULL, CONFIRM,
        SELECT_ROUTE, SKIP_TIME, DATE_RANGE_PICKER,
        DATE_PICKER, DISMISS, ENUM_VALUES, PLAYLIST_ITEM_SETTNGS,
        IMAGE_SEARCH, SUPPORT
    }

    class DismissDialogModel() : DialogModel(Type.DISMISS, 0)
}