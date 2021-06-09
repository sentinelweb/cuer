package uk.co.sentinelweb.cuer.app.ui.playlist_item_edit

import kotlinx.datetime.toJavaLocalDateTime
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel.Companion.PLAYLIST_SELECT_MODEL
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel.Type.PLAYLIST
import uk.co.sentinelweb.cuer.app.ui.common.dialog.AlertDialogModel
import uk.co.sentinelweb.cuer.app.ui.common.dialog.DialogModel
import uk.co.sentinelweb.cuer.app.ui.common.dialog.SelectDialogModel
import uk.co.sentinelweb.cuer.app.ui.common.mapper.BackgroundMapper
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper
import uk.co.sentinelweb.cuer.core.mappers.DateTimeFormatter
import uk.co.sentinelweb.cuer.core.mappers.TimeFormatter
import uk.co.sentinelweb.cuer.core.mappers.TimeFormatter.Format.SECS
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain

class PlaylistItemEditModelMapper(
    private val timeFormater: TimeFormatter,
    private val dateTimeFormater: DateTimeFormatter,
    private val res: ResourceWrapper,
    private val backgroundMapper: BackgroundMapper
) {

    fun map(
        domain: MediaDomain,
        selectedPlaylists: Set<PlaylistDomain>
    ) = PlaylistItemEditContract.Model(
        title = domain.title,
        description = domain.description,
        imageUrl = (domain.image ?: domain.thumbNail)?.url,
        channelTitle = domain.channelData.title,
        channelThumbUrl = (domain.channelData.thumbNail ?: domain.channelData.image)?.url,
        channelDescription = domain.channelData.description,
        chips = mutableListOf(PLAYLIST_SELECT_MODEL).apply {
            selectedPlaylists.forEachIndexed { index, playlist ->
                add(index, ChipModel(PLAYLIST, playlist.title, playlist.id.toString(), playlist.thumb ?: playlist.image))
            }
        },
        starred = domain.starred,
        canPlay = domain.platformId.isNotEmpty(),
        durationText = (
                if (domain.isLiveBroadcast) {
                    if (domain.isLiveBroadcastUpcoming) res.getString(R.string.upcoming)
                    else res.getString(R.string.live)
                } else domain.duration?.let { timeFormater.formatMillis(it, SECS) }
                ),
        positionText = domain.positon?.let { timeFormater.formatMillis(it, SECS) },
        position = domain.positon
            ?.takeIf { domain.duration != null && domain.duration!! > 0L }
            ?.let { (it / domain.duration!!).toFloat() },
        pubDate = dateTimeFormater.formatDateTimeNullable(domain.published?.toJavaLocalDateTime()),
        empty = false,
        isLive = domain.isLiveBroadcast,
        isUpcoming = domain.isLiveBroadcastUpcoming,
        infoTextBackgroundColor = backgroundMapper.mapInfoBackground(domain)
    )

    fun mapEmpty(): PlaylistItemEditContract.Model = PlaylistItemEditContract.Model(
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
        channelDescription = null,
        starred = false,
        canPlay = false,
        empty = true,
        isLive = false,
        isUpcoming = false,
        infoTextBackgroundColor = R.color.info_text_overlay_background
    )

    fun mapSaveConfirmAlert(confirm: () -> Unit, cancel: () -> Unit): AlertDialogModel = AlertDialogModel(
        R.string.dialog_title_save_check,
        R.string.dialog_message_save_check,
        AlertDialogModel.Button(R.string.dialog_button_save, confirm),
        cancel = AlertDialogModel.Button(R.string.dialog_button_dont_save, cancel),
    )

    fun mapItemSettings(item: MediaDomain, itemClick: (Int, Boolean) -> Unit, confirm: () -> Unit): SelectDialogModel = SelectDialogModel(
        DialogModel.Type.PLAYLIST_ITEM_SETTNGS,
        R.string.menu_settings,
        true,
        listOf(
            SelectDialogModel.Item("Watched", item.watched, true),
            SelectDialogModel.Item("Always play from start", item.playFromStart, true)
        ),
        itemClick,
        confirm,
        {}
    )

    companion object {
        private const val EMPTY_IMAGE = "file:///android_asset/sad_puppy.jpg"
    }
}
