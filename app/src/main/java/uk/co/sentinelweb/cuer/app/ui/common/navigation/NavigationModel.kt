package uk.co.sentinelweb.cuer.app.ui.common.navigation

import android.content.Intent
import android.os.Bundle
import androidx.navigation.NavOptions
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.*

data class NavigationModel constructor(
    val target: Target,
    val params: Map<Param, Any> = mapOf(),
    val navOpts: NavOptions? = null
) {
    init {
        params.keys
            .containsAll(target.requiredParams)
            .takeIf { it }
            ?: throw IllegalArgumentException("$target requires ${target.requiredParams}")
    }

    enum class Target constructor(
        val requiredParams: List<Param> = listOf(),
        val optionalParams: List<Param> = listOf()
    ) {
        LOCAL_PLAYER(listOf(MEDIA_ID)),
        WEB_LINK(listOf(LINK)),
        YOUTUBE_VIDEO(listOf(PLATFORM_ID)),
        YOUTUBE_CHANNEL(listOf(CHANNEL_ID)),
        MAIN_MEDIA(listOf(MEDIA)),
        MAIN_MEDIA_PLAY(listOf(MEDIA), listOf(PLAY_NOW)),
        SHARE(listOf(MEDIA)),
        PLAYLIST_EDIT(listOf(PLAYLIST_ID, SOURCE)),
        PLAYLIST_CREATE(),
        PLAYLIST_FRAGMENT(listOf(PLAYLIST_ID, SOURCE), listOf(PLAYLIST_ITEM_ID, PLAY_NOW)),
        PLAYLIST_ITEM_FRAGMENT(listOf(PLAYLIST_ITEM, SOURCE), listOf(FRAGMENT_NAV_EXTRAS)),
        PLAYLISTS_FRAGMENT(listOf(), listOf(PLAYLIST_ID)),
        BROWSE_FRAGMENT(),
        PLAYER_FRAGMENT(),
        NAV_BACK(), // use navigation to go back
        NAV_FINISH(), // use navigation to finish activity
        NAV_DONE(), // nav after operation is finished
        NAV_SEARCH()
        ;

        companion object {
            const val KEY = "Target"
        }
    }

    // todo add types as a class field - verify types in init
    enum class Param {
        MEDIA_ID, /* String */
        PLATFORM_ID, /* String */
        CHANNEL_ID, /* String */
        MEDIA, /* MediaDomain */
        LINK, /* String */
        PLAY_NOW, /* Boolean */
        PLAYLIST_ID, /* Long */
        PLAYLIST_ITEM_ID, /* Long */
        PLAYLIST_ITEM, /* PlaylistItemDomain */
        FRAGMENT_NAV_EXTRAS, /* FragmentNavigator.Extras */
        SOURCE, /* uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source */
        ;

        fun getLong(b: Bundle?) = b?.getLong(name)
        fun getLong(i: Intent?) = i?.let { if (it.hasExtra(name)) it.getLongExtra(name, -1) else null }
        fun getBoolean(b: Bundle?) = b?.getBoolean(name)
        fun getBoolean(i: Intent?) = i?.getBooleanExtra(name, false) ?: false
        fun getString(b: Bundle?) = b?.getString(name)
        fun getString(i: Intent?) = i?.getStringExtra(name)
        inline fun <reified T : Enum<T>> getEnum(b: Bundle?): T? =
            b?.getString(name)?.let { pref -> enumValues<T>().find { it.name == pref } }

        inline fun <reified T : Enum<T>> getEnum(i: Intent?): T? =
            i?.getStringExtra(name)?.let { pref -> enumValues<T>().find { it.name == pref } }
    }

}