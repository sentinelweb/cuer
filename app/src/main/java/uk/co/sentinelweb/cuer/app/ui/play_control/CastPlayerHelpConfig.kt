package uk.co.sentinelweb.cuer.app.ui.play_control

import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.common.resources.ActionResources
import uk.co.sentinelweb.cuer.app.ui.onboarding.OnboardingContract
import uk.co.sentinelweb.cuer.app.ui.onboarding.OnboardingContract.Config.Screen
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper

class CastPlayerHelpConfig(val res: ResourceWrapper) : OnboardingContract.ConfigBuilder {

    override fun build() = OnboardingContract.Config(
        screens = listOf(
            Screen(
                title = ActionResources(
                    label = "Player",
                    icon = R.drawable.ic_editable_items,
                    color = R.color.color_on_surface
                ),
                lines = listOf(
                    ActionResources(
                        label = "",
                        icon = R.drawable.ic_play,
                        color = R.color.color_on_surface
                    ),
                    ActionResources(
                        label = "",
                        icon = R.drawable.ic_playlist,
                        color = R.color.color_on_surface
                    ),
                    ActionResources(
                        label = "",
                        icon = R.drawable.ic_starred,
                        color = R.color.color_on_surface
                    ),
                    ActionResources(
                        label = "",
                        icon = R.drawable.ic_support,
                        color = R.color.color_on_surface
                    ),
                    ActionResources(
                        label = "",
                        icon = R.drawable.ic_edit,
                        color = R.color.color_on_surface
                    )
                )
            ),
            Screen(
                title = ActionResources(
                    label = "Playler 2",
                    icon = R.drawable.ic_editable_items,
                    color = R.color.color_on_surface
                ),
                lines = listOf(
                    ActionResources(
                        label = "",
                        icon = R.drawable.ic_share,
                        color = R.color.color_on_surface
                    ),
                    ActionResources(
                        label = "",
                        icon = R.drawable.ic_like_24,
                        color = R.color.color_on_surface
                    ),
                    ActionResources(
                        label = "",
                        icon = R.drawable.ic_comment,
                        color = R.color.color_on_surface
                    ),
                    ActionResources(
                        label = "",
                        icon = R.drawable.ic_launch,
                        color = R.color.color_on_surface
                    ),
                )
            ),
        )
    )
}