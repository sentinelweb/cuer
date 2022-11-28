package uk.co.sentinelweb.cuer.app.ui.playlist

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import uk.co.sentinelweb.cuer.app.ui.common.mapper.DurationTextColorMapper
import uk.co.sentinelweb.cuer.app.ui.common.resources.ActionResources
import uk.co.sentinelweb.cuer.app.ui.common.resources.StringDecoder
import uk.co.sentinelweb.cuer.app.ui.common.resources.StringResource
import uk.co.sentinelweb.cuer.core.mappers.Format
import uk.co.sentinelweb.cuer.core.mappers.TimeFormatter
import uk.co.sentinelweb.cuer.core.mappers.TimeSinceFormatter
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

class PlaylistMviItemModelMapper constructor(
    private val timeSinceFormatter: TimeSinceFormatter,
    private val timeFormatter: TimeFormatter,
    private val durationTextColorMapper: DurationTextColorMapper,
    private val stringDecoder: StringDecoder
) {

    fun mapItem(
        modelId: Long,
        item: PlaylistItemDomain,
        index: Int,
        canEdit: Boolean,
        canDelete: Boolean,
        canReorder: Boolean,
        playlistText: String?,
        showOverflow: Boolean,
        deleteResources: ActionResources?
    ): PlaylistItemMviContract.Model.Item {
        val top = "${item.media.title} : ${item.media.channelData.title}"
        val pos = item.media.positon?.toFloat() ?: 0f
        val progress = item.media.duration?.let { pos / it.toFloat() } ?: 0f
        return PlaylistItemMviContract.Model.Item(
            modelId,
            index = index,
            url = item.media.url,
            type = item.media.mediaType,
            title = top,
            duration = (
                    if (item.media.isLiveBroadcast) {
                        if (item.media.isLiveBroadcastUpcoming) stringDecoder.getString(StringResource.upcoming)
                        else stringDecoder.getString(StringResource.live)
                    } else (item.media.duration?.let {
                        item.media.duration?.let {
                            timeFormatter.formatMillis(
                                it,
                                Format.SECS
                            )
                        }
                    } ?: "-")
                    ),
            positon =
            if (item.media.isLiveBroadcast) stringDecoder.getString(StringResource.live)
            else (progress * 100).toInt().toString() + "%",
            thumbUrl = (item.media.thumbNail ?: item.media.image)?.url,
            imageUrl = (item.media.image ?: item.media.thumbNail)?.url,
            channelImageUrl = item.media.channelData.thumbNail?.url,
            progress = progress,
            isStarred = item.media.starred,
            watchedSince = item.media.dateLastPlayed?.let { timeSinceFormatter.formatTimeSince(it.toEpochMilliseconds()) }
                ?: "-",
            isWatched = item.media.watched,
            published = item.media.published
                ?.let { timeSinceFormatter.formatTimeSince(it.toInstant(TimeZone.UTC).epochSeconds) }// todo .convertToLocalMillis()
                ?: "-",
            platform = item.media.platform,
            isLive = item.media.isLiveBroadcast,
            isUpcoming = item.media.isLiveBroadcastUpcoming,
            infoTextBackgroundColor = durationTextColorMapper.mapInfoBackgroundItem(item.media),
            canEdit = canEdit,
            canDelete = canDelete,
            playlistName = playlistText,
            canReorder = canReorder,
            showOverflow = showOverflow,
            deleteResources = deleteResources
        )
    }
}
