package uk.co.sentinelweb.cuer.app.ui.playlist.item

import kotlinx.datetime.toJavaLocalDateTime
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.common.mapper.BackgroundMapper
import uk.co.sentinelweb.cuer.app.ui.resources.ActionResources
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper
import uk.co.sentinelweb.cuer.core.mappers.Format
import uk.co.sentinelweb.cuer.core.mappers.TimeFormatter
import uk.co.sentinelweb.cuer.core.mappers.TimeSinceFormatter
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import java.time.OffsetDateTime

class ItemModelMapper constructor(
    private val res: ResourceWrapper,
    private val timeSinceFormatter: TimeSinceFormatter,
    private val timeFormatter: TimeFormatter,
    private val backgroundMapper: BackgroundMapper
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
    ): ItemContract.Model {
        val top = "${item.media.title} : ${item.media.channelData.title}"
        val pos = item.media.positon?.toFloat() ?: 0f
        val progress = item.media.duration?.let { pos / it.toFloat() } ?: 0f
        return ItemContract.Model(
            modelId,
            index = index,
            url = item.media.url,
            type = item.media.mediaType,
            title = top,
            duration = (
                    if (item.media.isLiveBroadcast) {
                        if (item.media.isLiveBroadcastUpcoming) res.getString(R.string.upcoming)
                        else res.getString(R.string.live)
                    } else (item.media.duration?.let {
                        item.media.duration?.let {
                            timeFormatter.formatMillis(
                                it,
                                Format.SECS
                            )
                        }
                    } ?: "-")
                    ),
            positon = if (item.media.isLiveBroadcast) res.getString(R.string.live) else (progress * 100).toInt()
                .toString() + "%",
            thumbUrl = (item.media.thumbNail ?: item.media.image)?.url,
            imageUrl = (item.media.image ?: item.media.thumbNail)?.url,
            channelImageUrl = item.media.channelData.thumbNail?.url,
            progress = progress,
            starred = item.media.starred,
            watchedSince = item.media.dateLastPlayed?.let { timeSinceFormatter.formatTimeSince(it.toEpochMilliseconds()) }
                ?: "-",
            isWatched = item.media.watched,
            published = item.media.published?.let {
                timeSinceFormatter.formatTimeSince(
                    // todo shorten this
                    it.toJavaLocalDateTime().toInstant(OffsetDateTime.now().getOffset())
                        .toEpochMilli()
                )
            } ?: "-",
            platform = item.media.platform,
            isLive = item.media.isLiveBroadcast,
            isUpcoming = item.media.isLiveBroadcastUpcoming,
            infoTextBackgroundColor = backgroundMapper.mapInfoBackground(item.media),
            canEdit = canEdit,
            canDelete = canDelete,
            playlistName = playlistText,
            canReorder = canReorder,
            showOverflow = showOverflow,
            deleteResources = deleteResources
        )
    }
}