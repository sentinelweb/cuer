package uk.co.sentinelweb.cuer.app.ui.browse

import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.common.resources.ActionResources
import uk.co.sentinelweb.cuer.app.ui.onboarding.OnboardingContract
import uk.co.sentinelweb.cuer.app.ui.onboarding.OnboardingContract.Config.Screen
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper

class BrowseHelpConfig(val res: ResourceWrapper) : OnboardingContract.ConfigBuilder {

    override fun build(): OnboardingContract.Config {
        return OnboardingContract.Config(
            screens = listOf(
                Screen(
                    title = ActionResources(
                        label = res.getString(R.string.help_browse),
                        icon = R.drawable.ic_nav_browse
                    ),
                    subtitle = res.getString(R.string.help_browse_desc),
                    lines = listOf(
                        ActionResources(
                            label = res.getString(R.string.help_browse_az),
                            icon = R.drawable.ic_sort_by_alpha
                        ),
                        ActionResources(
                            label = res.getString(R.string.help_browse_categories),
                            icon = R.drawable.ic_category
                        ),
                        ActionResources(
                            label = res.getString(R.string.help_playlist_search),
                            icon = R.drawable.ic_search
                        ),
                        ActionResources(
                            label = res.getString(R.string.help_playlist_paste_add),
                            icon = R.drawable.ic_menu_paste_add
                        ),
                        ActionResources(
                            label = res.getString(R.string.help_playlist_settings),
                            icon = R.drawable.ic_menu_settings
                        ),
                        ActionResources(
                            label = "This help",
                            icon = R.drawable.ic_help
                        ),
                    )
                )
            )
        )
    }
}