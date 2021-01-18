package uk.co.sentinelweb.cuer.app.ui.common.navigation

import android.content.Intent
import android.os.Bundle
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.*

data class NavigationModel constructor(
    val target: Target,
    val params: Map<Param, Any> = mapOf()
) {
    init {
        params.keys
            .containsAll(target.requiredParams)
            .takeIf { it }
            ?: throw IllegalArgumentException("$target requires ${target.requiredParams}")
    }

    enum class Target constructor(
        val requiredParams: List<Param> = listOf()
    ) {
        LOCAL_PLAYER(listOf(MEDIA_ID)),
        WEB_LINK(listOf(LINK)),
        YOUTUBE_VIDEO(listOf(MEDIA_ID)),
        YOUTUBE_CHANNEL(listOf(CHANNEL_ID)),
        MAIN_MEDIA(listOf(MEDIA)),
        MAIN_MEDIA_PLAY(listOf(MEDIA, PLAY_NOW)),
        SHARE(listOf(MEDIA)),
        PLAYLIST_EDIT(listOf(PLAYLIST_ID)),
        PLAYLIST_CREATE(),
        PLAYLIST_FRAGMENT(listOf(PLAYLIST_ID, PLAY_NOW)),
        PLAYLIST_ITEM_FRAGMENT(listOf(PLAYLIST_ITEM, FRAGMENT_NAV_EXTRAS)),
        PLAYLISTS_FRAGMENT(),
        BROWSE_FRAGMENT(),
        PLAYER_FRAGMENT(),
        NAV_BACK(), // use navigation to go back
        ;

        companion object {
            const val KEY = "Target"
        }
    }

    // todo add types as a class field - verify types in init
    enum class Param {
        MEDIA_ID, /* String */
        CHANNEL_ID, /* String */
        MEDIA, /* MediaDomain */
        LINK, /* String */
        PLAY_NOW, /* Boolean */
        PLAYLIST_ID, /* Long */
        PLAYLIST_ITEM_ID, /* Long */
        PLAYLIST_ITEM, /* PlaylistItemDomain */
        FRAGMENT_NAV_EXTRAS /**/
        ;

        fun getLong(b: Bundle?) = b?.getLong(name)
        fun getLong(i: Intent?) = i?.let { if (it.hasExtra(name)) it.getLongExtra(name, -1) else null }
        fun getBoolean(b: Bundle?) = b?.getBoolean(name)
        fun getBoolean(i: Intent?) = i?.getBooleanExtra(name, false) ?: false
        fun getString(b: Bundle?) = b?.getString(name)
        fun getString(i: Intent?) = i?.getStringExtra(name)
    }

}