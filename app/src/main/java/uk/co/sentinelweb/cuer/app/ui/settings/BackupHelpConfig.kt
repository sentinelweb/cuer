package uk.co.sentinelweb.cuer.app.ui.settings

import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.onboarding.OnboardingContract
import uk.co.sentinelweb.cuer.app.ui.onboarding.OnboardingContract.Config.Screen
import uk.co.sentinelweb.cuer.app.ui.resources.ActionResources
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper

class BackupHelpConfig(val res: ResourceWrapper) : OnboardingContract.ConfigBuilder {

    override fun build() = OnboardingContract.Config(
        screens = listOf(
            Screen(
                title = ActionResources(
                    label = "Playlist edit",
                    icon = R.drawable.ic_playlist_edit,
                    color = R.color.color_on_surface
                ),
                lines = listOf(
                    ActionResources(
                        label = "Save / Create",
                        icon = R.drawable.ic_button_tick_24_white,
                        color = R.color.color_on_surface
                    ),
                    ActionResources(
                        label = "Star",
                        icon = R.drawable.ic_starred,
                        color = R.color.color_on_surface
                    ),
                    ActionResources(
                        label = "Pin",
                        icon = R.drawable.ic_push_pin_on,
                        color = R.color.color_on_surface
                    ),
                    ActionResources(
                        label = "Change the title text",
                        icon = R.drawable.ic_edit_24,
                        color = R.color.color_on_surface
                    )
                )
            ),
            Screen(
                title = ActionResources(
                    label = "Playlist options",
                    icon = R.drawable.ic_playlist_edit,
                    color = R.color.color_on_surface
                ),
                lines = listOf(
                    ActionResources(
                        label = "Default: Used when nothing selcted",
                        icon = R.drawable.ic_playlist_default,
                        color = R.color.color_on_surface
                    ),
                    ActionResources(
                        label = "Play from start (all items)",
                        icon = R.drawable.ic_play_start_black,
                        color = R.color.color_on_surface
                    ),
                    ActionResources(
                        label = "Playable",
                        icon = R.drawable.ic_play_black,
                        color = R.color.color_on_surface
                    ),
                    ActionResources(
                        label = "Deleteable",
                        icon = R.drawable.ic_delete_white,
                        color = R.color.color_on_surface
                    ),
                    ActionResources(
                        label = "Deleteable Items",
                        icon = R.drawable.ic_launch_black,
                        color = R.color.color_on_surface
                    ),
                    ActionResources(
                        label = "Editable items",
                        icon = R.drawable.ic_launch_black,
                        color = R.color.color_on_surface
                    ),
                )
            ),
        )
    )
}
