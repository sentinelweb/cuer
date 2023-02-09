package uk.co.sentinelweb.cuer.app.ui.playlist

import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.common.resources.ActionResources
import uk.co.sentinelweb.cuer.app.ui.onboarding.OnboardingContract
import uk.co.sentinelweb.cuer.app.ui.onboarding.OnboardingContract.Config.Screen
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper

class PlaylistHelpConfig(val res: ResourceWrapper) : OnboardingContract.ConfigBuilder {

    override fun build() = OnboardingContract.Config(
        screens = listOf(
            Screen(
                title = ActionResources(
                    label = res.getString(R.string.help_playlist_title_gestures),
                    icon = R.drawable.ic_playlist
                ),
                lines = listOf(
                    ActionResources(
                        label = res.getString(R.string.help_playlist_delete),
                        icon = R.drawable.ic_delete_item
                    ),
                    ActionResources(
                        label = res.getString(R.string.help_playlist_move),
                        icon = R.drawable.ic_move_24
                    ),
                    ActionResources(
                        label = res.getString(R.string.help_playlist_reorder),
                        icon = R.drawable.ic_up_down
                    )
                )
            ),
            Screen(
                title = ActionResources(
                    label = res.getString(R.string.help_playlist_title_actions),
                    icon = R.drawable.ic_playlists
                ),
                lines = listOf(
                    ActionResources(
                        label = res.getString(R.string.help_playlist_play),
                        icon = R.drawable.ic_playlist_play
                    ),
                    ActionResources(
                        label = res.getString(R.string.help_playlist_stop),
                        icon = R.drawable.ic_playlist_close
                    ),
                    ActionResources(
                        label = res.getString(R.string.help_playlist_edit),
                        icon = R.drawable.ic_edit_24
                    ),
                    ActionResources(
                        label = res.getString(R.string.help_playlist_star),
                        icon = R.drawable.ic_starred
                    ),
                    ActionResources(
                        label = res.getString(R.string.help_playlist_playmode),
                        icon = R.drawable.ic_playmode_straight
                    )
                )
            ),
            Screen(
                title = ActionResources(
                    label = res.getString(R.string.help_playlist_title_actions_top),
                    icon = R.drawable.ic_playlists
                ),
                lines = listOf(
                    ActionResources(
                        label = res.getString(R.string.help_playlist_viewmode_cards),
                        icon = R.drawable.ic_view_card
                    ),
                    ActionResources(
                        label = res.getString(R.string.help_playlist_viewmode_rows),
                        icon = R.drawable.ic_view_list
                    ),
                    ActionResources(
                        label = res.getString(R.string.help_playlist_search),
                        icon = R.drawable.ic_search
                    ),
                    ActionResources(
                        label = res.getString(R.string.help_playlist_paste_add),
                        icon = R.drawable.ic_menu_paste_add_black
                    ),
                    ActionResources(
                        label = res.getString(R.string.help_playlist_settings),
                        icon = R.drawable.ic_menu_settings
                    ),
                )
            ),
            Screen(
                title = ActionResources(
                    label = res.getString(R.string.help_playlist_title_status_icons),
                    icon = R.drawable.ic_playlists
                ),
                lines = listOf(
                    ActionResources(
                        label = res.getString(R.string.help_playlist_pos),
                        icon = null
                    ),
                    ActionResources(
                        label = res.getString(R.string.help_playlist_starred),
                        icon = R.drawable.ic_starred
                    ),
                    ActionResources(
                        label = res.getString(R.string.help_playlist_children),
                        icon = R.drawable.ic_tree_24
                    ),
                    ActionResources(
                        label = res.getString(R.string.help_playlist_default),
                        icon = R.drawable.ic_playlist_default
                    ),
                    ActionResources(
                        label = res.getString(R.string.help_playlist_playstart),
                        icon = R.drawable.ic_play_start_black
                    ),
                    ActionResources(
                        label = res.getString(R.string.help_playlist_pinned),
                        icon = R.drawable.ic_push_pin_on
                    )
                )
            )
        )
    )
}