package uk.co.sentinelweb.cuer.app.ui.playlist_item_edit

import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper
import uk.co.sentinelweb.cuer.core.mappers.TimeFormatter
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import java.time.chrono.IsoChronology
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.FormatStyle
import java.util.*

class PlaylistItemEditModelMapper(
    private val timeFormater: TimeFormatter,
    private val res: ResourceWrapper
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
        selectedPlaylists: Set<PlaylistDomain>
    ) = PlaylistItemEditModel(
        title = domain.title,
        description = domain.description,
        imageUrl = (domain.image ?: domain.thumbNail)?.url,
        channelTitle = domain.channelData.title,
        channelThumbUrl = (domain.channelData.thumbNail ?: domain.channelData.image)?.url,
        chips = mutableListOf(ChipModel(ChipModel.Type.PLAYLIST_SELECT)).apply {
            selectedPlaylists.forEachIndexed { index, playlist ->
                add(
                    index,
                    ChipModel(ChipModel.Type.PLAYLIST, playlist.title, playlist.id.toString(), playlist.thumb ?: playlist.image)
                )
            }
        },
        starred = domain.starred,
        canPlay = domain.platformId.isNotEmpty(),
        durationText = domain.duration?.let { timeFormater.formatMillis(it, TimeFormatter.Format.SECS) },
        positionText = domain.positon?.let { timeFormater.formatMillis(it, TimeFormatter.Format.SECS) },
        position = domain.positon
            ?.takeIf { domain.duration != null && domain.duration!! > 0L }
            ?.let { (it / domain.duration!!).toFloat() }
        ,
        pubDate = pubDateFormatter.format(domain.published),
        empty = false
    )

    fun mapEmpty(): PlaylistItemEditModel = PlaylistItemEditModel(
        title = res.getString(R.string.pie_empty_title),
        imageUrl = EMPTY_IMAGE,
        description = res.getString(R.string.pie_empty_desc),
        pubDate = null,
        position = -1f,
        positionText = null,
        durationText = null,
        chips = listOf(),
        channelTitle = null,
        channelThumbUrl = null,
        starred = false,
        canPlay = false,
        empty = true
    )

    companion object {
        private const val EMPTY_IMAGE = "file:///android_asset/sad_puppy.jpg"
    }
}
