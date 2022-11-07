package uk.co.sentinelweb.cuer.app.ui.onboarding

import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.onboarding.OnboardingContract.Config.Screen
import uk.co.sentinelweb.cuer.app.ui.resources.ActionResources

val onboardingConfig = OnboardingContract.Config(
    screens = listOf(
        Screen(
            title = "Sharing",
            lines = listOf(
                ActionResources(
                    label = "From your tube app",
                    icon = R.drawable.ic_platform_youtube,
                    color = R.color.color_on_surface
                ),
                ActionResources(
                    label = "Share a video or playlist with Cuer",
                    icon = R.drawable.ic_share_black,
                    color = R.color.color_on_surface
                )
            )
        ),
        Screen(
            title = "Playlists",
            lines = listOf(
                ActionResources(
                    label = "From your tube app",
                    icon = R.drawable.ic_platform_youtube,
                    color = R.color.color_on_surface
                ),
                ActionResources(
                    label = "Share a video or playlist with Cuer",
                    icon = R.drawable.ic_share_black,
                    color = R.color.color_on_surface
                )
            )
        )
    )
)