package uk.co.sentinelweb.cuer.app.ui.onboarding

import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.common.resources.ActionResources
import uk.co.sentinelweb.cuer.app.ui.onboarding.OnboardingContract.Config.Screen
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper

class AppInstallHelpConfig(val res: ResourceWrapper) : OnboardingContract.ConfigBuilder {

    override fun build() = OnboardingContract.Config(
        screens = listOf(
            Screen(
                backgroundUrl = "https://cuer-275020.firebaseapp.com/images/categories/Socrates.jpg",
                backgroundColor = R.color.black,
                title = ActionResources(
                    label = "Welcome to Cuer",
                    icon = null,
                    color = R.color.primary
                ),
                lines = listOf(
                    ActionResources(
                        label = "Discover the essence of philosophy and broaden your mind with our curated collection of engaging and informative philosophy videos",
                        icon = null,
                        color = R.color.white
                    ),
                    ActionResources(
                        label = "Browse the collection of philosophers",
                        icon = R.drawable.ic_nav_browse,
                        color = R.color.white
                    ),
                    ActionResources(
                        label = "Click to add them to your playlists",
                        icon = R.drawable.ic_playlists,
                        color = R.color.white
                    ),
                    ActionResources(
                        label = "You can watch all the playlist via chromecast or on your device",
                        icon = R.drawable.ic_playlist,
                        color = R.color.white
                    ),
                    ActionResources(
                        label = "Use the support button to support the creators",
                        icon = R.drawable.ic_support,
                        color = R.color.white
                    )
                )
            ),
            Screen(
                backgroundUrl = "https://cuer-275020.firebaseapp.com/images/headers/filip-kominik-IHtVbLRjTZU-unsplash.jpg",
                title = ActionResources(
                    label = "Playlists",
                    icon = R.drawable.ic_playlists,
                    color = R.color.white
                ),
                lines = listOf(
                    ActionResources(
                        label = "You can also make your own playlists from video sites like YouTube",
                        icon = R.drawable.ic_platform_youtube,
                        color = R.color.white
                    ),
                    ActionResources(
                        label = "Share playlists or videos with Cuer",
                        icon = R.drawable.ic_share_black,
                        color = R.color.white
                    ),
                    ActionResources(
                        label = "Or copy links from apps or search and use the paste/add action",
                        icon = R.drawable.ic_menu_paste_add_black,
                        color = R.color.white
                    ),
                    ActionResources(
                        label = "We'll add more platforms soon ....",
                        icon = null,
                        color = R.color.white
                    ),
                )
            )
        )
    )
}