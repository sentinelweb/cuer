package uk.co.sentinelweb.cuer.app.usecase

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import uk.co.sentinelweb.cuer.app.queue.QueueMediatorContract
import uk.co.sentinelweb.cuer.app.ui.common.dialog.AlertDialogContract
import uk.co.sentinelweb.cuer.app.ui.common.dialog.AlertDialogModel
import uk.co.sentinelweb.cuer.app.ui.common.resources.StringDecoder
import uk.co.sentinelweb.cuer.app.ui.common.resources.StringResource
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract
import uk.co.sentinelweb.cuer.app.ui.ytplayer.floating.FloatingPlayerContract
import uk.co.sentinelweb.cuer.app.util.chromecast.listener.ChromecastContract
import uk.co.sentinelweb.cuer.app.util.cuercast.CuerCastPlayerWatcher
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferencesWrapper
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.domain.PlaylistAndItemDomain

class PlayUseCase(
    private val queue: QueueMediatorContract.Producer,
    private val ytCastContextHolder: ChromecastContract.PlayerContextHolder,
    private val prefsWrapper: MultiPlatformPreferencesWrapper,
    private val coroutines: CoroutineContextProvider,
    private val floatingService: FloatingPlayerContract.Manager,
    private val strings: StringDecoder,
    private val alertDialogCreator: AlertDialogContract.Creator,
    private val cuerCastPlayerWatcher: CuerCastPlayerWatcher,
) : KoinComponent {

    interface Dialog {
        var playUseCase: PlayUseCase?
        fun showPlayDialog(playlistAndItem: PlaylistAndItemDomain)
    }

    fun createPlayDialog(): Dialog = getKoin().get<Dialog>().apply {
        playUseCase = this@PlayUseCase
    }

    fun playLogic(
        playlistAndItem: PlaylistAndItemDomain,
        resetPos: Boolean
    ) {
        if (floatingService.isRunning()) {
            playlistAndItem.also { floatingService.playItem(it) }
        } else if (ytCastContextHolder.isConnected()) {
            playlistAndItem
                .let { playItem(it, resetPos) }
            // fixme enable when can play via cuer cast
//        } else if (cuerCastPlayerWatcher.isWatching()) {
//            playlistAndItem
//                .let { playItem(it, resetPos) }
        } else {
            createPlayDialog().showPlayDialog(playlistAndItem)
        }
    }

    private fun playItem(itemDomain: PlaylistAndItemDomain, resetPos: Boolean) {
        if (queue.playlistId == itemDomain.playlistId) {
            queue.onItemSelected(itemDomain.item, resetPosition = resetPos)
        } else {
            alertDialogCreator.createAndShowDialog(mapChangePlaylistAlert({
                prefsWrapper.currentPlayingPlaylistId = itemDomain.playlistId!!
                coroutines.computationScope.launch {
                    queue.switchToPlaylist(itemDomain.playlistId!!)
                    queue.onItemSelected(itemDomain.item, forcePlay = true, resetPosition = resetPos)
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
            neutral = AlertDialogModel.Button(StringResource.cancel, info)
        )

    fun setQueueItem(playlistAndItem: PlaylistAndItemDomain) {
        coroutines.computationScope.launch {
            val toIdentifier = playlistAndItem.playlistId
                ?: throw IllegalArgumentException("item is not in a playlist")
            queue.switchToPlaylist(toIdentifier)
            queue.onItemSelected(playlistAndItem.item, forcePlay = true, resetPosition = false)
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
