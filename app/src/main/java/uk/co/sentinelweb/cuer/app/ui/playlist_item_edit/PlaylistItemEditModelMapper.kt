package uk.co.sentinelweb.cuer.app.ui.playlist_item_edit

import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel
import uk.co.sentinelweb.cuer.app.ui.common.dialog.SelectDialogModel
import uk.co.sentinelweb.cuer.core.mappers.DateTimeMapper
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import java.time.chrono.IsoChronology
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.FormatStyle
import java.util.*

class PlaylistItemEditModelMapper(
    private val dateTimeMapper: DateTimeMapper
) {
    var pattern: String = DateTimeFormatterBuilder
        .getLocalizedDateTimePattern(
            FormatStyle.SHORT
            , FormatStyle.SHORT
            , IsoChronology.INSTANCE
            , Locale.getDefault()
        )
    private val pubDateFormatter = DateTimeFormatter.ofPattern(pattern)

    fun map(
        domain: MediaDomain,
        selectedPlaylists: MutableSet<PlaylistDomain>
    ) = PlaylistItemEditModel(
        title = domain.title,
        description = domain.description,
        imageUrl = (domain.image ?: domain.thumbNail)?.url,
        channelTitle = domain.channelData.title,
        channelThumbUrl = (domain.channelData.thumbNail ?: domain.channelData.image)?.url,
        chips = mutableListOf(ChipModel(ChipModel.Type.PLAYLIST_SELECT)).apply {
            selectedPlaylists.forEachIndexed { index, playlist ->
                add(index, ChipModel(ChipModel.Type.PLAYLIST, playlist.title, playlist.id))
            }
        },
        starred = domain.starred,
        canPlay = domain.mediaId.isNotEmpty(),
        durationText = domain.duration?.let { dateTimeMapper.formatTime(it) },
        positionText = domain.positon?.let { dateTimeMapper.formatTime(it) },
        position = domain.positon
            ?.takeIf { domain.duration != null && domain.duration!! > 0L }
            ?.let { (it / domain.duration!!).toFloat() }
        ,
        pubDate = pubDateFormatter.format(domain.published)
    )

    fun mapSelection(all: List<PlaylistDomain>, selected: Set<PlaylistDomain>): SelectDialogModel =
        SelectDialogModel(
            type = SelectDialogModel.Type.PLAYLIST,
            title = "Select Playlist",
            items = all.map { playlist ->
                SelectDialogModel.Item(
                    playlist.title,
                    selected = (selected.find { sel -> playlist.title == sel.title } != null)
                )
            }
        )


}