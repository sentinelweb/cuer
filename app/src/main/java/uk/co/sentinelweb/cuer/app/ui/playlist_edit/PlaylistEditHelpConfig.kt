package uk.co.sentinelweb.cuer.app.ui.playlist_edit

import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.onboarding.OnboardingContract
import uk.co.sentinelweb.cuer.app.ui.onboarding.OnboardingContract.Config.Screen
import uk.co.sentinelweb.cuer.app.ui.resources.ActionResources
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper

class PlaylistEditHelpConfig(val res: ResourceWrapper) : OnboardingContract.ConfigBuilder {

    override fun build() = OnboardingContract.Config(
        screens = listOf(
            Screen(
                title = ActionResources(
                    label = res.getString(R.string.help_pe_title),
                    icon = R.drawable.ic_playlist_edit,
                    color = R.color.color_on_surface
                ),
                lines = listOf(
                    ActionResources(
                        label = res.getString(R.string.help_pe_save),
                        icon = R.drawable.ic_button_tick_24_white,
                        color = R.color.color_on_surface
                    ),
                    ActionResources(
                        label = res.getString(R.string.help_pe_star),
                        icon = R.drawable.ic_starred,
                        color = R.color.color_on_surface
                    ),
                    ActionResources(
                        label = res.getString(R.string.help_pe_pin),
                        icon = R.drawable.ic_push_pin_on,
                        color = R.color.color_on_surface
                    ),
                    ActionResources(
                        label = res.getString(R.string.help_pe_title_text),
                        icon = R.drawable.ic_edit_24,
                        color = R.color.color_on_surface
                    )
                )
            ),
            Screen(
                title = ActionResources(
                    label = res.getString(R.string.help_pe_title_options),
                    icon = R.drawable.ic_playlist_edit,
                    color = R.color.color_on_surface
                ),
                lines = listOf(
                    ActionResources(
                        label = res.getString(R.string.help_pe_default),
                        icon = R.drawable.ic_playlist_default,
                        color = R.color.color_on_surface
                    ),
                    ActionResources(
                        label = res.getString(R.string.help_pe_play_start),
                        icon = R.drawable.ic_play_start_black,
                        color = R.color.color_on_surface
                    ),
                    ActionResources(
                        label = res.getString(R.string.help_pe_playable),
                        icon = R.drawable.ic_play_black,
                        color = R.color.color_on_surface
                    ),
                    ActionResources(
                        label = res.getString(R.string.help_pe_deletable),
                        icon = R.drawable.ic_delete_white,
                        color = R.color.color_on_surface
                    ),
                    ActionResources(
                        label = res.getString(R.string.help_pe_deletable_items),
                        icon = R.drawable.ic_delete_white,
                        color = R.color.color_on_surface
                    ),
                    ActionResources(
                        label = res.getString(R.string.help_pe_editabe_items),
                        icon = R.drawable.ic_launch_black,
                        color = R.color.color_on_surface
                    ),
                )
            ),
        )
    )
}
