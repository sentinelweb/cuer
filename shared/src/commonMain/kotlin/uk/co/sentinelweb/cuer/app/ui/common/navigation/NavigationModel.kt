package uk.co.sentinelweb.cuer.app.ui.common.navigation

import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.LINK
import uk.co.sentinelweb.cuer.app.ui.onboarding.OnboardingContract
import uk.co.sentinelweb.cuer.domain.*
import kotlin.reflect.KClass

data class NavigationModel(
    val target: Target,
    val params: Map<Param, Any?> = mapOf(),
//    val navOpts: NavOptions? = null
) {
    init {
        params.keys
            .containsAll(target.requiredParams)
            .takeIf { it }
            ?.also {
                params.forEach {
                    if (it.value != null && it.key.type != it.value!!::class)
                        throw IllegalArgumentException(
                            "Invalid parameter type: ${it.key} requires ${it.key.type} - was ${it.value!!::class}"
                        )
                }
            }
            ?: throw IllegalArgumentException("$target requires ${target.requiredParams}")
    }

    inline fun <reified T> getParam(p: Param): T? = params[p] as T

    enum class Target(
        val requiredParams: List<Param> = listOf(),
        @Suppress("unused") val optionalParams: List<Param> = listOf()
    ) {
        LOCAL_PLAYER_FULL(listOf(Param.PLAYLIST_AND_ITEM)),
        LOCAL_PLAYER(listOf(Param.PLAYLIST_AND_ITEM)),
        WEB_LINK(listOf(LINK)),
        SHARE(listOf(LINK)),
        CRYPTO_LINK(listOf(Param.CRYPTO_ADDRESS)),
        YOUTUBE_VIDEO(listOf(Param.PLATFORM_ID)),
        YOUTUBE_VIDEO_POS(listOf(Param.PLAYLIST_ITEM)),
        YOUTUBE_CHANNEL(listOf(Param.CHANNEL_ID)),
        PLAYLIST(listOf(Param.PLAYLIST_ID, Param.SOURCE), listOf(Param.PLAYLIST_ITEM_ID, Param.PLAY_NOW)),
        PLAYLIST_ITEM(listOf(Param.PLAYLIST_ITEM, Param.SOURCE)), //, listOf(FRAGMENT_NAV_EXTRAS)
        PLAYLISTS(listOf()),
        PLAYLIST_EDIT(listOf(Param.PLAYLIST_ID, Param.SOURCE), listOf()),
        PLAYLIST_CREATE(listOf(Param.SOURCE), listOf()),
        FOLDER_LIST(listOf(Param.REMOTE_ID), listOf(Param.FILE_PATH)),
        BROWSE,
        NAV_BACK, // use navigation to go back
        NAV_FINISH, // use navigation to finish activity
        NAV_DONE, // nav after operation is finished
        NAV_NONE // use to clear navigation e.g. in live data state
        ;


        companion object {
            const val KEY = "Target"
        }
    }

    enum class Param(
        val type: KClass<*>
    ) {
        PLATFORM_ID(String::class), // platform id
        CHANNEL_ID(String::class), // platform id
        LINK(String::class),
        CRYPTO_ADDRESS(LinkDomain.CryptoLinkDomain::class),
        PLAY_NOW(Boolean::class),
        PLAYLIST_ID(String::class), // guid
        PLAYLIST_ITEM_ID(String::class), // guid
        PLAYLIST_ITEM((PlaylistItemDomain::class)),
        PLAYLIST_AND_ITEM((PlaylistAndItemDomain::class)),
        REMOTE_ID(String::class), // guid

        //FRAGMENT_NAV_EXTRAS(FragmentNavigator.Extras::class),
        SOURCE(OrchestratorContract.Source::class),
        HEADLESS(Boolean::class),
        PLAYLIST_PARENT(String::class),
        CATEGORY(CategoryDomain::class),
        PASTE(Boolean::class),
        IMAGE_URL(String::class),
        MEDIA(MediaDomain::class),
        APP_LIST(List::class),
        ALLOW_PLAY(Boolean::class),
        DO_AUTO_BACKUP(Boolean::class),
        ONBOARD_CONFIG(OnboardingContract.Config::class),
        ONBOARD_KEY(String::class),
        BACK_PARAMS(Int::class),
        FILE_PATH(String::class)
        ;
    }

    companion object {
        val BACK = NavigationModel(Target.NAV_BACK)
        val DONE = NavigationModel(Target.NAV_DONE)
        val FINISH = NavigationModel(Target.NAV_FINISH)
    }

}
