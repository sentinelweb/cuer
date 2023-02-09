package uk.co.sentinelweb.cuer.app.ui.playlist.item

import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Identifier
import uk.co.sentinelweb.cuer.app.ui.common.ktx.convertToLocalMillis
import uk.co.sentinelweb.cuer.app.ui.common.mapper.DurationTextColorMapper
import uk.co.sentinelweb.cuer.app.ui.common.resources.ActionResources
import uk.co.sentinelweb.cuer.app.ui.playlist.PlaylistItemMviContract
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper
import uk.co.sentinelweb.cuer.core.mappers.Format
import uk.co.sentinelweb.cuer.core.mappers.TimeFormatter
import uk.co.sentinelweb.cuer.core.mappers.TimeSinceFormatter
import uk.co.sentinelweb.cuer.domain.GUID
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

class ItemModelMapper constructor(
    private val res: ResourceWrapper,
    private val timeSinceFormatter: TimeSinceFormatter,
    private val timeFormatter: TimeFormatter,
    private val durationTextColorMapper: DurationTextColorMapper
) {

    fun mapItem(
        modelId: Identifier<GUID>,
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
            isStarred = item.media.starred,
            watchedSince = item.media.dateLastPlayed?.let { timeSinceFormatter.formatTimeSince(it.toEpochMilliseconds()) }
                ?: "-",
            isWatched = item.media.watched,
            published = item.media.published
                ?.let { timeSinceFormatter.formatTimeSince(it.convertToLocalMillis()) }
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