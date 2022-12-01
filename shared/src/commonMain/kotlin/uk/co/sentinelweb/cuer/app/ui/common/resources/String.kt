package uk.co.sentinelweb.cuer.app.ui.common.resources

enum class StringResource(val default: String) {
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

    // browse
    // todo move browse strings here

    // playlists
    playlists_section_app("App"),
    // todo move playlists strings here

    // playlist
    playlist_error_please_add("Please add the playlist first"),
    playlist_error_updating("Error updating: %1\$s …"),
    playlist_items_updated("%1\$s new items"),

    // alert dialogs
    dialog_title_save_check("Save playlist"),
    dialog_message_save_item_check("Do you want to save this playlist"),
    dialog_button_save("Save"),
    dialog_button_dont_save("Don't Save"),
    dialog_button_view_info("View info"),
    playlist_change_dialog_title("Change playlist?"),
    playlist_change_dialog_message("This will change the current playlist …"),

    // backup
    pref_backup_restore_auto_confirm(""),

    // Crypto
    support_crypto_warning_ok("Crypto warning"),

}

interface StringDecoder {
    fun getString(res: StringResource): String
    fun getString(res: StringResource, params: List<String>): String
}