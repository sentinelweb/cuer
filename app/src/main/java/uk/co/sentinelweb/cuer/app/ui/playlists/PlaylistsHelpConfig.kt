package uk.co.sentinelweb.cuer.app.ui.playlists

import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.onboarding.OnboardingContract
import uk.co.sentinelweb.cuer.app.ui.onboarding.OnboardingContract.Config.Screen
import uk.co.sentinelweb.cuer.app.ui.resources.ActionResources
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper

class PlaylistsHelpConfig(val res: ResourceWrapper) : OnboardingContract.ConfigBuilder {

    override fun build() = OnboardingContract.Config(
        screens = listOf(
            Screen(
                title = ActionResources(
                    label = res.getString(R.string.help_playlists_title_app),
                    icon = R.drawable.ic_playlist_black,
                    color = R.color.color_on_surface
                ),
                lines = listOf(
                    ActionResources(
                        label = res.getString(R.string.help_playlists_app_new),
                        icon = null,
                        color = R.color.color_on_surface
                    ),
                    ActionResources(
                        label = res.getString(R.string.help_playlists_app_recent),
                        icon = null,
                        color = R.color.color_on_surface
                    ),
                    ActionResources(
                        label = res.getString(R.string.help_playlists_app_starred),
                        icon = null,
                        color = R.color.color_on_surface
                    ),
                    ActionResources(
                        label = res.getString(R.string.help_playlists_app_unfinished),
                        icon = null,
                        color = R.color.color_on_surface
                    ),
                    ActionResources(
                        label = res.getString(R.string.help_playlists_app_local),
                        icon = null,
                        color = R.color.color_on_surface
                    ),
                    ActionResources(
                        label = res.getString(R.string.help_playlists_app_yt),
                        icon = null,
                        color = R.color.color_on_surface
                    )
                )
            ),
            Screen(
                title = ActionResources(
                    label = res.getString(R.string.help_playlists_title_actionbar),
                    icon = R.drawable.ic_playlist_black,
                    color = R.color.color_on_surface
                ),
                lines = listOf(
                    ActionResources(
                        label = res.getString(R.string.help_playlists_ab_create),
                        icon = R.drawable.ic_playlist_add,
                        color = R.color.color_on_surface
                    ),
                    ActionResources(
                        label = res.getString(R.string.help_playlists_ab_pasteadd),
                        icon = R.drawable.ic_menu_paste_add_black,
                        color = R.color.color_on_surface
                    ),
                )
            ),
        )
    )
}
