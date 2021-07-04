package uk.co.sentinelweb.cuer.app.ui.common.views.description

import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel

interface DescriptionContract {

    data class DescriptionModel constructor(
        val title: CharSequence?,
        val description: String?,
        val chips: List<ChipModel> = listOf(ChipModel.PLAYLIST_SELECT_MODEL),
        val channelTitle: String?,
        val channelThumbUrl: String?,
        val channelDescription: String?,
        val pubDate: String?
    )

    interface Interactions {
        fun onLinkClick(urlString: String)
        fun onChannelClick()

        fun onSelectPlaylistChipClick(@Suppress("UNUSED_PARAMETER") model: ChipModel)
        fun onRemovePlaylist(chipModel: ChipModel)
    }

    companion object {
        val viewModule = module {
            factory { DescriptionMapper(get()) }
        }
    }
}