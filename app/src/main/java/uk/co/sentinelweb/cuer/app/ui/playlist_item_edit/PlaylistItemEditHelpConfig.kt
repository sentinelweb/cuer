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
                    icon = R.drawable.ic_playlist_item
                ),
                lines = listOf(
                    ActionResources(
                        label = res.getString(R.string.help_plie_play),
                        icon = R.drawable.ic_play
                    ),
                    ActionResources(
                        label = res.getString(R.string.help_plie_select_playlist),
                        icon = R.drawable.ic_playlist
                    ),
                    ActionResources(
                        label = res.getString(R.string.help_plie_star),
                        icon = R.drawable.ic_starred
                    ),
                    ActionResources(
                        label = res.getString(R.string.help_plie_support),
                        icon = R.drawable.ic_support
                    ),
                    ActionResources(
                        label = res.getString(R.string.help_plie_edit),
                        icon = R.drawable.ic_edit
                    ),
                    ActionResources(
                        label = res.getString(R.string.help_plie_share),
                        icon = R.drawable.ic_share
                    ),
                    ActionResources(
                        label = res.getString(R.string.help_plie_like),
                        icon = R.drawable.ic_like_24
                    ),
                    ActionResources(
                        label = res.getString(R.string.help_plie_comment),
                        icon = R.drawable.ic_comment
                    ),
                    ActionResources(
                        label = res.getString(R.string.help_plie_launch),
                        icon = R.drawable.ic_launch
                    ),
                )
            )
        )
    )
}
