package uk.co.sentinelweb.cuer.app.ui.common.navigation

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
        val requiredParams: List<Param>
    ) {
        LOCAL_PLAYER(listOf(MEDIA_ID)),
        WEB_LINK(listOf(LINK)),
        YOUTUBE_VIDEO(listOf(MEDIA_ID)),
        YOUTUBE_CHANNEL(listOf(CHANNEL_ID)),
        MAIN_MEDIA(listOf(MEDIA)),
        MAIN_MEDIA_PLAY(listOf(MEDIA, PLAY_NOW)),
        SHARE(listOf(MEDIA)),
        PLAYLIST_EDIT(listOf(PLAYLIST_ID)),
        PLAYLIST_CREATE(listOf()),
        PLAYLIST_FRAGMENT(listOf(PLAYLIST_ID, PLAY_NOW)),
        PLAYLISTS_FRAGMENT(listOf()),
        BROWSE_FRAGMENT(listOf()),
        PLAYER_FRAGMENT(listOf())

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
        ;

        fun getLong(b: Bundle?) = b?.getLong(toString())
        fun getBoolean(b: Bundle?) = b?.getBoolean(toString())
        fun getString(b: Bundle?) = b?.getString(toString())
    }


}