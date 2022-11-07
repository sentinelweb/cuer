package uk.co.sentinelweb.cuer.app.ui.onboarding

import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.onboarding.OnboardingContract.Config.Screen
import uk.co.sentinelweb.cuer.app.ui.resources.ActionResources

val onboardingConfig = OnboardingContract.Config(
    screens = listOf(
        Screen(
            title = ActionResources(
                label = "Sharing",
                icon = R.drawable.ic_share_black,
                color = R.color.color_on_surface
            ),
            lines = listOf(
                ActionResources(
                    label = "From your tube app",
                    icon = R.drawable.ic_platform_youtube,
                    color = R.color.primary_on
                ),
                ActionResources(
                    label = "Share a video or playlist with Cuer",
                    icon = R.drawable.ic_share_black,
                    color = R.color.color_on_surface
                )
            )
        ),
        Screen(
            title = ActionResources(
                label = "Playlist",
                icon = R.drawable.ic_playlists,
                color = R.color.color_on_surface
            ),
            lines = listOf(
                ActionResources(
                    label = "New",
                    icon = R.drawable.ic_platform_youtube,
                    color = R.color.color_on_surface
                ),
                ActionResources(
                    label = "Recent",
                    icon = R.drawable.ic_share_black,
                    color = R.color.color_on_surface
                )
            )
        )
    )
)