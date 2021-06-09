package uk.co.sentinelweb.cuer.app.ui.playlist

import kotlinx.datetime.toJavaLocalDateTime
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL
import uk.co.sentinelweb.cuer.app.ui.common.dialog.AlertDialogModel
import uk.co.sentinelweb.cuer.app.ui.common.mapper.BackgroundMapper
import uk.co.sentinelweb.cuer.app.ui.common.mapper.IconMapper
import uk.co.sentinelweb.cuer.app.ui.playlist.item.ItemContract
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper
import uk.co.sentinelweb.cuer.core.mappers.TimeFormatter
import uk.co.sentinelweb.cuer.core.mappers.TimeFormatter.Format.SECS
import uk.co.sentinelweb.cuer.core.mappers.TimeSinceFormatter
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.PlaylistTreeDomain
import java.time.OffsetDateTime


class PlaylistModelMapper constructor(
    private val res: ResourceWrapper,
    private val timeSinceFormatter: TimeSinceFormatter,
    private val timeFormatter: TimeFormatter,
    private val iconMapper: IconMapper,
    private val backgroundMapper: BackgroundMapper
) {
    private var _modelIdGenerator = 0L
    var modelIdGenerator: Long = 0
        get() {
            _modelIdGenerator--
            return _modelIdGenerator
        }
        set(value) = if (value != 0L) {
            throw IllegalArgumentException("You can only reset the generator")
        } else field = value

    fun map(
        domain: PlaylistDomain,
        isPlaying: Boolean,
        mapItems: Boolean = true,
        id: OrchestratorContract.Identifier<*>,
        pinned: Boolean,
        playlists: Map<Long, PlaylistTreeDomain>?
    ): PlaylistContract.Model {
        modelIdGenerator = 0
        val itemsIdMap = mutableMapOf<Long, PlaylistItemDomain>()
        return PlaylistContract.Model(
            title = domain.title,
            imageUrl = domain.image?.url ?: "gs://cuer-275020.appspot.com/playlist_header/headphones-2588235_640.jpg",
            loopModeIndex = domain.mode.ordinal,
            loopModeIcon = iconMapper.map(domain.mode),
            playIcon = if (isPlaying) R.drawable.ic_baseline_playlist_close_24 else R.drawable.ic_baseline_playlist_play_24,
            starredIcon = if (domain.starred) R.drawable.ic_button_starred_white else R.drawable.ic_button_unstarred_white,
            isDefault = domain.default,
            isSaved = id.source == LOCAL,
            isPlayFromStart = domain.playItemsFromStart,
            isPinned = pinned,
            canPlay = domain.config.playable,
            canEdit = domain.config.editable,
            items = if (mapItems) {
                domain.items.mapIndexed { index, item ->
                    val modelId = item.id ?: modelIdGenerator
                    itemsIdMap[modelId] = item
                    mapItem(
                        modelId,
                        item,
                        index,
                        domain.config.editableItems,
                        domain.config.deletableItems,
                        domain.config.editable,
                        playlists,
                        domain.id
                    )
                }
            } else {
                null
            },
            hasChildren = playlists?.get(domain.id)?.chidren?.size ?: 0,
            itemsIdMap = itemsIdMap
        )
    }

    fun mapItem(
        modelId: Long,
        item: PlaylistItemDomain,
        index: Int,
        canEdit: Boolean,
        canDelete: Boolean,
        canReorder: Boolean,
        playlists: Map<Long, PlaylistTreeDomain>?,
        currentPlaylistId: Long?
    ): ItemContract.Model {
        val top = item.media.title ?: "No title"
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
                    } else (item.media.duration?.let { item.media.duration?.let { timeFormatter.formatMillis(it, SECS) } } ?: "-")
                    ),
            positon = if (item.media.isLiveBroadcast) res.getString(R.string.live) else (progress * 100).toInt().toString() + "%",
            thumbNailUrl = item.media.thumbNail?.url,
            progress = progress,
            starred = item.media.starred,
            watchedSince = item.media.dateLastPlayed?.let { timeSinceFormatter.formatTimeSince(it.toEpochMilliseconds()) } ?: "-",
            isWatched = item.media.watched,
            published = item.media.published?.let {
                timeSinceFormatter.formatTimeSince(
                    // todo shorten this
                    it.toJavaLocalDateTime().toInstant(OffsetDateTime.now().getOffset()).toEpochMilli()
                )
            } ?: "-",
            platform = item.media.platform,
            isLive = item.media.isLiveBroadcast,
            isUpcoming = item.media.isLiveBroadcastUpcoming,
            infoTextBackgroundColor = backgroundMapper.mapInfoBackground(item.media),
            canEdit = canEdit,
            canDelete = canDelete,
            playlistName = item.playlistId?.let {
                if (it != currentPlaylistId) playlists?.get(it)?.node?.title else null
            },
            canReorder = canReorder
        )
    }

    fun mapChangePlaylistAlert(confirm: () -> Unit, info: () -> Unit): AlertDialogModel = AlertDialogModel(
        R.string.playlist_change_dialog_title,
        R.string.playlist_change_dialog_message,
        AlertDialogModel.Button(R.string.ok, confirm),
        AlertDialogModel.Button(R.string.dialog_button_view_info, info)
    )

}
