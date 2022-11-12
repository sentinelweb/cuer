package uk.co.sentinelweb.cuer.app.ui.browse

import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.onboarding.OnboardingContract
import uk.co.sentinelweb.cuer.app.ui.onboarding.OnboardingContract.Config.Screen
import uk.co.sentinelweb.cuer.app.ui.resources.ActionResources
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper

class BrowseHelpConfig(val res: ResourceWrapper) : OnboardingContract.ConfigBuilder {

    override fun build(): OnboardingContract.Config {
        return OnboardingContract.Config(
            screens = listOf(
                Screen(
                    title = ActionResources(
                        label = res.getString(R.string.help_browse),
                        icon = R.drawable.ic_editable_items,
                        color = R.color.color_on_surface
                    ),
                    lines = listOf(
                        ActionResources(
                            label = res.getString(R.string.help_browse_desc),
                            icon = R.drawable.ic_play_black,
                            color = R.color.color_on_surface
                        ),
                        ActionResources(
                            label = res.getString(R.string.help_browse_az),
                            icon = R.drawable.ic_sort_by_alpha,
                            color = R.color.color_on_surface
                        ),
                        ActionResources(
                            label = res.getString(R.string.help_browse_categories),
                            icon = R.drawable.ic_category,
                            color = R.color.color_on_surface
                        ),
                        ActionResources(
                            label = res.getString(R.string.help_playlist_search),
                            icon = R.drawable.ic_search,
                            color = R.color.color_on_surface
                        ),
//                        ActionResources(
//                            label = res.getString(R.string.help_playlist_paste_add),
//                            icon = R.drawable.ic_menu_paste_add_black,
//                            color = R.color.color_on_surface
//                        ),
                        ActionResources(
                            label = res.getString(R.string.help_playlist_settings),
                            icon = R.drawable.ic_menu_settings_black,
                            color = R.color.color_on_surface
                        ),
                    )
                )
            )
        )
    }
}