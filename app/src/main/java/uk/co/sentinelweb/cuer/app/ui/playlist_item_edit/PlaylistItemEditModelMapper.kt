package uk.co.sentinelweb.cuer.app.ui.playlist_item_edit

import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.common.dialog.AlertDialogModel
import uk.co.sentinelweb.cuer.app.ui.common.dialog.DialogModel
import uk.co.sentinelweb.cuer.app.ui.common.dialog.SelectDialogModel
import uk.co.sentinelweb.cuer.app.ui.common.mapper.BackgroundMapper
import uk.co.sentinelweb.cuer.app.ui.common.views.description.DescriptionMapper
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper
import uk.co.sentinelweb.cuer.core.mappers.Format.SECS
import uk.co.sentinelweb.cuer.core.mappers.TimeFormatter
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain

class PlaylistItemEditModelMapper(
    private val timeFormater: TimeFormatter,
    private val descriptionMapper: DescriptionMapper,
    private val res: ResourceWrapper,
    private val backgroundMapper: BackgroundMapper
) {

    fun map(
        domain: MediaDomain,
        selectedPlaylists: Set<PlaylistDomain>
    ) = PlaylistItemEditContract.Model(
        description = descriptionMapper.map(domain, selectedPlaylists),
        imageUrl = (domain.image ?: domain.thumbNail)?.url,
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
        empty = false,
        isLive = domain.isLiveBroadcast,
        isUpcoming = domain.isLiveBroadcastUpcoming,
        infoTextBackgroundColor = backgroundMapper.mapInfoBackground(domain)
    )

    fun mapEmpty(): PlaylistItemEditContract.Model = PlaylistItemEditContract.Model(
        description = descriptionMapper.mapEmpty(),
        imageUrl = EMPTY_IMAGE,
        position = -1f,
        positionText = null,
        durationText = null,
        starred = false,
        canPlay = false,
        empty = true,
        isLive = false,
        isUpcoming = false,
        infoTextBackgroundColor = R.color.info_text_overlay_background
    )

    fun mapSaveConfirmAlert(confirm: () -> Unit, cancel: () -> Unit): AlertDialogModel = AlertDialogModel(
        R.string.dialog_title_save_check,
        R.string.dialog_message_save_item_check,
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
