package uk.co.sentinelweb.cuer.app.ui.playlist_edit

import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.common.resources.ActionResources
import uk.co.sentinelweb.cuer.app.ui.onboarding.OnboardingContract
import uk.co.sentinelweb.cuer.app.ui.onboarding.OnboardingContract.Config.Screen
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper

class PlaylistEditHelpConfig(val res: ResourceWrapper) : OnboardingContract.ConfigBuilder {

    override fun build() = OnboardingContract.Config(
        screens = listOf(
            Screen(
                title = ActionResources(
                    label = res.getString(R.string.help_pe_title),
                    icon = R.drawable.ic_edit
                ),
                subtitle = "Edit playlist title and properties",
                lines = listOf(
                    ActionResources(
                        label = res.getString(R.string.help_pe_save),
                        icon = R.drawable.ic_button_tick_24_white
                    ),
                    ActionResources(
                        label = res.getString(R.string.help_pe_star),
                        icon = R.drawable.ic_starred
                    ),
                    ActionResources(
                        label = res.getString(R.string.help_pe_pin),
                        icon = R.drawable.ic_push_pin_on
                    ),
                    ActionResources(
                        label = res.getString(R.string.help_pe_title_text),
                        icon = null
                    ),
                    ActionResources(
                        label = res.getString(R.string.help_pe_default),
                        icon = R.drawable.ic_playlist_default
                    ),
                    ActionResources(
                        label = res.getString(R.string.help_pe_play_start),
                        icon = R.drawable.ic_play_start_black
                    ),
                )
            ),
            Screen(
                title = ActionResources(
                    label = res.getString(R.string.help_pe_title_options),
                    icon = R.drawable.ic_edit
                ),
                lines = listOf(
                    ActionResources(
                        label = res.getString(R.string.help_pe_playable),
                        icon = R.drawable.ic_play
                    ),
                    ActionResources(
                        label = res.getString(R.string.help_pe_deletable),
                        icon = R.drawable.ic_delete
                    ),
                    ActionResources(
                        label = res.getString(R.string.help_pe_deletable_items),
                        icon = R.drawable.ic_delete_item
                    ),
                    ActionResources(
                        label = res.getString(R.string.help_pe_editabe_items),
                        icon = R.drawable.ic_editable_items
                    ),
                    ActionResources(
                        label = res.getString(R.string.help_pe_parent),
                        icon = R.drawable.ic_tree
                    ),
                    ActionResources(
                        label = "Mark all items as (un)watched",
                        icon = R.drawable.ic_visibility
                    ),
                )
            ),
        )
    )
}
