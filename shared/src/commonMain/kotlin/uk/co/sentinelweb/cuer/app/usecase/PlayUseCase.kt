package uk.co.sentinelweb.cuer.app.usecase

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.queue.QueueMediatorContract
import uk.co.sentinelweb.cuer.app.ui.common.dialog.AlertDialogModel
import uk.co.sentinelweb.cuer.app.ui.common.resources.StringDecoder
import uk.co.sentinelweb.cuer.app.ui.common.resources.StringResource
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract
import uk.co.sentinelweb.cuer.app.ui.ytplayer.floating.FloatingPlayerContract
import uk.co.sentinelweb.cuer.app.util.cast.listener.CastPlayerContextHolder
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferencesWrapper
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

class PlayUseCase constructor(
    private val queue: QueueMediatorContract.Producer,
    private val ytCastContextHolder: CastPlayerContextHolder,
    private val prefsWrapper: MultiPlatformPreferencesWrapper,
    private val coroutines: CoroutineContextProvider,
    private val floatingService: FloatingPlayerContract.Manager,
    private val playDialog: Dialog,
    private val strings: StringDecoder,
) {

    interface Dialog {
        var playUseCase: PlayUseCase
        fun showPlayDialog(item: PlaylistItemDomain?, playlistTitle: String?)
        fun showDialog(model: AlertDialogModel)
    }

    init {
        playDialog.playUseCase = this
    }

    fun playLogic(
        itemDomain: PlaylistItemDomain?,
        playlist: PlaylistDomain?,
        resetPos: Boolean
    ) {
        val item = (itemDomain ?: queue.currentItem)
        if (floatingService.isRunning()) {
            item?.also { floatingService.playItem(it) }
        } else if (ytCastContextHolder.isConnected()) {
            itemDomain
                ?.let { playItem(it, resetPos) }
        } else {
            playDialog.showPlayDialog(itemDomain, playlist?.title)
        }
    }

    private fun playItem(itemDomain: PlaylistItemDomain, resetPos: Boolean) {
        if (queue.playlistId == itemDomain.playlistId) {
            queue.onItemSelected(itemDomain, resetPosition = resetPos)
        } else {
            playDialog.showDialog(mapChangePlaylistAlert({

                prefsWrapper.currentPlayingPlaylistId = itemDomain.playlistId!!
                coroutines.computationScope.launch {
                    queue.switchToPlaylist(itemDomain.playlistId!!)
                    queue.onItemSelected(itemDomain, forcePlay = true, resetPosition = resetPos)
                }
            }, {/*cancel*/ }
            ))
        }
    }

    private fun mapChangePlaylistAlert(confirm: () -> Unit, info: () -> Unit): AlertDialogModel =
        AlertDialogModel(
            title = strings.get(StringResource.playlist_change_dialog_title),
            message = strings.get(StringResource.playlist_change_dialog_message),
            confirm = AlertDialogModel.Button(StringResource.ok, confirm),
            neutral = AlertDialogModel.Button(StringResource.dialog_button_view_info, info)
        )

    fun setQueueItem(item: PlaylistItemDomain) {
        coroutines.computationScope.launch {
            val toIdentifier = item.playlistId
                ?: throw IllegalArgumentException("item is not in a playlist")
            queue.switchToPlaylist(toIdentifier)
            queue.onItemSelected(item, forcePlay = true, resetPosition = false)
        }
    }

    fun attachControls(playerControls: PlayerContract.PlayerControls?) {
        // todo call after service starts !! :(
        coroutines.mainScope.launch {
            delay(500)
            floatingService.get()?.external?.mainPlayerControls = playerControls
        }
    }
}