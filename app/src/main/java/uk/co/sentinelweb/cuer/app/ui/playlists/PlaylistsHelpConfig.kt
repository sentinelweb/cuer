package uk.co.sentinelweb.cuer.app.ui.playlists

import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.common.resources.ActionResources
import uk.co.sentinelweb.cuer.app.ui.onboarding.OnboardingContract
import uk.co.sentinelweb.cuer.app.ui.onboarding.OnboardingContract.Config.Screen
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper

class PlaylistsHelpConfig(val res: ResourceWrapper) : OnboardingContract.ConfigBuilder {

    override fun build() = OnboardingContract.Config(
        screens = listOf(
            Screen(
                title = ActionResources(
                    label = "Playlists - Sections",
                    icon = R.drawable.ic_playlist
                ),
                lines = listOf(
                    ActionResources(
                        label = "App: playlists generated by app"
                    ),
                    ActionResources(
                        label = "Recent: recent playlists used"
                    ),
                    ActionResources(
                        label = "Starred: Starred playlists"
                    ),
                    ActionResources(
                        label = "All playlists (tree)"
                    ),
                    ActionResources(
                        label = "Swipe RIGHT to change parent",
                        icon = R.drawable.ic_tree_24
                    ),
                    ActionResources(
                        label = "Swipe Left to delete",
                        icon = R.drawable.ic_delete
                    )
                )
            ),
            Screen(
                title = ActionResources(
                    label = res.getString(R.string.help_playlists_title_app),
                    icon = R.drawable.ic_playlist
                ),
                lines = listOf(
                    ActionResources(
                        label = res.getString(R.string.help_playlists_app_new)
                    ),
                    ActionResources(
                        label = res.getString(R.string.help_playlists_app_recent)
                    ),
                    ActionResources(
                        label = res.getString(R.string.help_playlists_app_starred)
                    ),
                    ActionResources(
                        label = res.getString(R.string.help_playlists_app_unfinished)
                    ),
                    ActionResources(
                        label = res.getString(R.string.help_playlists_app_local)
                    ),
                    ActionResources(
                        label = res.getString(R.string.help_playlists_app_yt)
                    )
                )
            ),
            Screen(
                title = ActionResources(
                    label = res.getString(R.string.help_playlists_title_actionbar),
                    icon = R.drawable.ic_playlist
                ),
                lines = listOf(
                    ActionResources(
                        label = res.getString(R.string.help_playlists_ab_create),
                        icon = R.drawable.ic_playlist_add
                    ),
                    ActionResources(
                        label = res.getString(R.string.help_playlists_ab_pasteadd),
                        icon = R.drawable.ic_menu_paste_add_black
                    ),
                    ActionResources(
                        label = res.getString(R.string.help_playlist_search),
                        icon = R.drawable.ic_search
                    ),
                    ActionResources(
                        label = res.getString(R.string.help_playlist_settings),
                        icon = R.drawable.ic_menu_settings
                    ),
                )
            ),
        )
    )
}
