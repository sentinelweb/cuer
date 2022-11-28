package uk.co.sentinelweb.cuer.app.ui.playlist_item_edit

import android.os.Build
import android.text.style.ImageSpan
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.common.dialog.AlertDialogModel
import uk.co.sentinelweb.cuer.app.ui.common.dialog.DialogModel
import uk.co.sentinelweb.cuer.app.ui.common.dialog.SelectDialogModel
import uk.co.sentinelweb.cuer.app.ui.common.ktx.convertToLocalMillis
import uk.co.sentinelweb.cuer.app.ui.common.mapper.DurationTextColorMapper
import uk.co.sentinelweb.cuer.app.ui.common.resources.StringResource
import uk.co.sentinelweb.cuer.app.ui.common.ribbon.RibbonCreator
import uk.co.sentinelweb.cuer.app.ui.common.views.description.DescriptionMapper
import uk.co.sentinelweb.cuer.app.ui.playlist.item.ItemTextMapper
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper
import uk.co.sentinelweb.cuer.core.mappers.Format.SECS
import uk.co.sentinelweb.cuer.core.mappers.TimeFormatter
import uk.co.sentinelweb.cuer.core.mappers.TimeSinceFormatter
import uk.co.sentinelweb.cuer.domain.MediaDomain

class PlaylistItemEditModelMapper(
    private val timeFormater: TimeFormatter,
    private val descriptionMapper: DescriptionMapper,
    private val res: ResourceWrapper,
    private val durationTextColorMapper: DurationTextColorMapper,
    private val ribbonCreator: RibbonCreator,
    private val itemTextMapper: ItemTextMapper,
    private val timeSinceFormatter: TimeSinceFormatter,
) {

    fun map(state: PlaylistItemEditContract.State) = with(state) {
        media?.let { media ->
            PlaylistItemEditContract.Model(
                description = descriptionMapper.map(
                    domain = media,
                    selectedPlaylists = selectedPlaylists,
                    editablePlaylists = !isOnSharePlaylist,
                    ribbonActions = ribbonCreator.createRibbon(media)
                ),
                imageUrl = (media.image ?: media.thumbNail)?.url,
                starred = media.starred,
                canPlay = media.platformId.isNotEmpty(),
                showPlay = allowPlay,
                durationText = (
                        if (media.isLiveBroadcast) {
                            if (media.isLiveBroadcastUpcoming) res.getString(R.string.upcoming)
                            else res.getString(R.string.live)
                        } else media.duration?.let { timeFormater.formatMillis(it, SECS) }
                        ),
                positionText = media.positon?.let { timeFormater.formatMillis(it, SECS) },
                position = getPositionRatio(media),
                empty = false,
                isLive = media.isLiveBroadcast,
                isUpcoming = media.isLiveBroadcastUpcoming,
                infoTextColor = res.getColorResourceId(durationTextColorMapper.mapInfoText(media)),
                itemText = itemTextMapper.buildBottomText(
                    posText = getPositionText(media),
                    watchedText = media.dateLastPlayed
                        ?.let { timeSinceFormatter.formatTimeSince(it.toEpochMilliseconds()) }
                        ?: "-",
                    publishedText = media.published
                        ?.let { timeSinceFormatter.formatTimeSince(it.convertToLocalMillis()) }
                        ?: "-",
                    platformDomain = media.platform,
                    isStarred = media.starred,
                    isWatched = media.watched,
                    align = if (Build.VERSION.SDK_INT >= 29) ImageSpan.ALIGN_CENTER else ImageSpan.ALIGN_BASELINE,
                )
            )
        } ?: mapEmpty()
    }

    private fun getPositionRatio(media: MediaDomain) =
        media.positon
            ?.takeIf { media.duration != null && media.duration!! > 0L }
            ?.let { (it.toFloat() / media.duration!!) }

    private fun getPositionText(media: MediaDomain) =
        if (media.isLiveBroadcast) ""
        else ((getPositionRatio(media) ?: 0f) * 100).toInt().toString() + "%"

    fun mapEmpty(): PlaylistItemEditContract.Model = PlaylistItemEditContract.Model(
        description = descriptionMapper.mapEmpty(),
        imageUrl = EMPTY_IMAGE,
        position = -1f,
        positionText = null,
        durationText = null,
        starred = false,
        canPlay = false,
        showPlay = false,
        empty = true,
        isLive = false,
        isUpcoming = false,
        itemText = "",
        infoTextColor = R.color.white
    )

    fun mapSaveConfirmAlert(confirm: () -> Unit, cancel: () -> Unit): AlertDialogModel =
        AlertDialogModel(
            title = res.getString(R.string.dialog_title_save_check),
            message = res.getString(R.string.dialog_message_save_item_check),
            confirm = AlertDialogModel.Button(StringResource.dialog_button_save, confirm),
            cancel = AlertDialogModel.Button(StringResource.dialog_button_dont_save, cancel),
        )

    fun mapItemSettings(
        item: MediaDomain,
        itemClick: (Int, Boolean) -> Unit,
        confirm: () -> Unit
    ): SelectDialogModel = SelectDialogModel(
        DialogModel.Type.PLAYLIST_ITEM_SETTNGS,
        res.getString(R.string.menu_settings),
        true,
        listOf(
            SelectDialogModel.Item(res.getString(R.string.pie_watched), item.watched, true),
            SelectDialogModel.Item(res.getString(R.string.pie_play_start), item.playFromStart, true)
        ),
        itemClick,
        confirm,
        {}
    )


    companion object {
        private const val EMPTY_IMAGE = "file:///android_asset/sad_puppy.jpg"
    }
}
