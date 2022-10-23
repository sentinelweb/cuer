package uk.co.sentinelweb.cuer.app.ui.common.navigation

import android.content.Intent
import android.os.Bundle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.FragmentNavigator
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.*
import uk.co.sentinelweb.cuer.domain.CategoryDomain
import uk.co.sentinelweb.cuer.domain.LinkDomain
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import kotlin.reflect.KClass

data class NavigationModel constructor(
    val target: Target,
    val params: Map<Param, Any?> = mapOf(),
    val navOpts: NavOptions? = null
) {
    init {
        params.keys
            .containsAll(target.requiredParams)
            .takeIf { it }
            ?.also {
                params.forEach {
                    if (it.value != null && it.key.type != it.value!!::class)
                        throw IllegalArgumentException("Invalid parameter type: ${it.key} requires ${it.key.type} - was ${it.value!!::class}"
                    )
                }
            }
            ?: throw IllegalArgumentException("$target requires ${target.requiredParams}")
    }

    inline fun <reified T> getParam(p:Param): T? = params[p] as T

    enum class Target constructor(
        val requiredParams: List<Param> = listOf(),
        @Suppress("unused") val optionalParams: List<Param> = listOf()
    ) {
        LOCAL_PLAYER_FULL(listOf(Param.PLAYLIST_ITEM)),
        LOCAL_PLAYER(listOf(Param.PLAYLIST_ITEM)),
        WEB_LINK(listOf(LINK)),
        SHARE(listOf(LINK)),
        CRYPTO_LINK(listOf(CRYPTO_ADDRESS)),
        YOUTUBE_VIDEO(listOf(PLATFORM_ID)),
        YOUTUBE_VIDEO_POS(listOf(Param.PLAYLIST_ITEM)),
        YOUTUBE_CHANNEL(listOf(CHANNEL_ID)),
        PLAYLIST(listOf(PLAYLIST_ID, SOURCE), listOf(PLAYLIST_ITEM_ID, PLAY_NOW)),
        PLAYLIST_ITEM(listOf(Param.PLAYLIST_ITEM, SOURCE), listOf(FRAGMENT_NAV_EXTRAS)),
        PLAYLISTS(listOf()),
        PLAYLIST_EDIT(listOf(PLAYLIST_ID, SOURCE), listOf()),
        PLAYLIST_CREATE(listOf(SOURCE), listOf()),
        BROWSE,
        NAV_BACK, // use navigation to go back
        NAV_FINISH, // use navigation to finish activity
        NAV_DONE, // nav after operation is finished
        ;


        companion object {
            const val KEY = "Target"
        }
    }

    enum class Param(
        val type: KClass<*>
    ) {
        PLATFORM_ID(String::class),
        CHANNEL_ID(String::class),
        LINK(String::class),
        CRYPTO_ADDRESS(LinkDomain.CryptoLinkDomain::class),
        PLAY_NOW(Boolean::class),
        PLAYLIST_ID(Long::class),
        PLAYLIST_ITEM_ID(Long::class),
        PLAYLIST_ITEM((PlaylistItemDomain::class)),
        FRAGMENT_NAV_EXTRAS(FragmentNavigator.Extras::class),
        SOURCE(OrchestratorContract.Source::class),
        HEADLESS(Boolean::class),
        PLAYLIST_PARENT(Long::class),
        CATEGORY(CategoryDomain::class),
        PASTE(Boolean::class),
        IMAGE_URL(String::class),
        MEDIA(MediaDomain::class),
        APP_LIST(List::class),
        ALLOW_PLAY(Boolean::class),
        AUTO_BACKUP(Boolean::class),
        ;

        fun getLong(b: Bundle?) = b?.getLong(name)
        fun getLong(i: Intent?) =
            i?.let { if (it.hasExtra(name)) it.getLongExtra(name, -1) else null }

        fun getBoolean(b: Bundle?, def: Boolean = false) = b?.getBoolean(name, def) ?: def
        fun getBoolean(i: Intent?) = i?.getBooleanExtra(name, false) ?: false

        fun getString(b: Bundle?) = b?.getString(name)
        fun getString(i: Intent?) = i?.getStringExtra(name)

        inline fun <reified T : Enum<T>> getEnum(b: Bundle?): T? =
            b?.getString(name)?.let { pref -> enumValues<T>().find { it.name == pref } }

        inline fun <reified T : Enum<T>> getEnum(i: Intent?): T? =
            i?.getStringExtra(name)?.let { pref -> enumValues<T>().find { it.name == pref } }
    }

    companion object {
        val BACK = NavigationModel(Target.NAV_BACK)
        val DONE = NavigationModel(Target.NAV_DONE)
        val FINISH = NavigationModel(Target.NAV_FINISH)
    }

}