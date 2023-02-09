package uk.co.sentinelweb.cuer.app.ui.playlist_item_edit

import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.common.resources.ActionResources
import uk.co.sentinelweb.cuer.app.ui.onboarding.OnboardingContract
import uk.co.sentinelweb.cuer.app.ui.onboarding.OnboardingContract.Config.Screen
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper

class PlaylistItemEditHelpConfig(val res: ResourceWrapper) : OnboardingContract.ConfigBuilder {

    override fun build() = OnboardingContract.Config(
        screens = listOf(
            Screen(
                title = ActionResources(
                    label = res.getString(R.string.help_plie_title_1),
                    icon = R.drawable.ic_playlist_item,
                    color = R.color.color_on_surface
                ),
                lines = listOf(
                    ActionResources(
                        label = res.getString(R.string.help_plie_play),
                        icon = R.drawable.ic_play_black,
                        color = R.color.color_on_surface
                    ),
                    ActionResources(
                        label = res.getString(R.string.help_plie_select_playlist),
                        icon = R.drawable.ic_playlist,
                        color = R.color.color_on_surface
                    ),
                    ActionResources(
                        label = res.getString(R.string.help_plie_star),
                        icon = R.drawable.ic_starred,
                        color = R.color.color_on_surface
                    ),
                    ActionResources(
                        label = res.getString(R.string.help_plie_support),
                        icon = R.drawable.ic_support,
                        color = R.color.color_on_surface
                    ),
                    ActionResources(
                        label = res.getString(R.string.help_plie_edit),
                        icon = R.drawable.ic_edit_24,
                        color = R.color.color_on_surface
                    )
                )
            ),
            Screen(
                title = ActionResources(
                    label = res.getString(R.string.help_plie_title_2),
                    icon = R.drawable.ic_playlist_item,
                    color = R.color.color_on_surface
                ),
                lines = listOf(
                    ActionResources(
                        label = res.getString(R.string.help_plie_share),
                        icon = R.drawable.ic_share_black,
                        color = R.color.color_on_surface
                    ),
                    ActionResources(
                        label = res.getString(R.string.help_plie_like),
                        icon = R.drawable.ic_like_24,
                        color = R.color.color_on_surface
                    ),
                    ActionResources(
                        label = res.getString(R.string.help_plie_comment),
                        icon = R.drawable.ic_comment_24,
                        color = R.color.color_on_surface
                    ),
                    ActionResources(
                        label = res.getString(R.string.help_plie_launch),
                        icon = R.drawable.ic_launch_black,
                        color = R.color.color_on_surface
                    ),
                )
            ),
        )
    )
}
