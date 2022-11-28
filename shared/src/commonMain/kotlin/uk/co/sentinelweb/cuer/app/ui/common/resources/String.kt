package uk.co.sentinelweb.cuer.app.ui.common.resources

enum class StringResource(val default: String) {
    playlists_section_app("App"),
    menu_playlist_mode_single("Single"),
    menu_playlist_mode_loop("Loop"),
    menu_playlist_mode_shuffle("Shuffle"),
    stop("Stop"),
    menu_play("Play"),
    menu_unstar("Unstar"),
    menu_star("Star"),
    upcoming("Upcoming"),
    live("LIVE"),
    ok("OK"),
    cancel("Cancel"),

    // alert dialogs
    dialog_title_save_check("Save playlist"),
    dialog_message_save_item_check("Do you want to save this playlist"),
    dialog_button_save("Save"),
    dialog_button_dont_save("Don't Save"),
    dialog_button_view_info("View info"),

    // backup
    pref_backup_restore_auto_confirm(""),

    // Crypto
    support_crypto_warning_ok("Crypto warning"),

}

interface StringDecoder {
    fun getString(res: StringResource): String
}