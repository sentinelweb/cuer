package uk.co.sentinelweb.cuer.app.ui.common.views.description

import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel
import uk.co.sentinelweb.cuer.app.ui.common.ribbon.RibbonModel
import uk.co.sentinelweb.cuer.app.ui.common.views.description.DescriptionContract.DescriptionModel
import uk.co.sentinelweb.cuer.core.mappers.DateTimeFormatter
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain

class DescriptionMapper constructor(
    private val dateTimeFormater: DateTimeFormatter
) {
    fun map(
        domain: MediaDomain,
        selectedPlaylists: Set<PlaylistDomain>?,
        editablePlaylists: Boolean = true,
        ribbonActions: List<RibbonModel>
    ): DescriptionModel = DescriptionModel(
        title = domain.title,
        description = domain.description,
        channelTitle = domain.channelData.title,
        channelThumbUrl = (domain.channelData.thumbNail ?: domain.channelData.image)?.url,
        channelDescription = domain.channelData.description,
        playlistChips = mutableListOf<ChipModel>()
            .apply { if (editablePlaylists) add(ChipModel.PLAYLIST_SELECT_MODEL) }
            .apply {
                selectedPlaylists
                    ?.forEachIndexed { index, playlist ->
                        add(index, chipModel(playlist, editablePlaylists))
                    }
            },
        pubDate = dateTimeFormater.formatDateTimeNullable(domain.published),
        ribbonActions = ribbonActions
    )

    private fun chipModel(playlist: PlaylistDomain, editablePlaylists: Boolean) = ChipModel(
        ChipModel.Type.PLAYLIST,
        playlist.title,
        playlist.id.toString(),
        playlist.thumb ?: playlist.image,
        editablePlaylists
    )

    fun mapEmpty() = DescriptionModel(
        title = null,
        description = null,
        pubDate = null,
        playlistChips = listOf(),
        channelTitle = null,
        channelThumbUrl = null,
        channelDescription = null,
        ribbonActions = listOf()
    )
}
