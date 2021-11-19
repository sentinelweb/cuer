package uk.co.sentinelweb.cuer.app.usecase

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL
import uk.co.sentinelweb.cuer.app.orchestrator.toIdentifier
import uk.co.sentinelweb.cuer.app.orchestrator.toPairType
import uk.co.sentinelweb.cuer.app.queue.QueueMediatorContract
import uk.co.sentinelweb.cuer.app.ui.common.dialog.AlertDialogModel
import uk.co.sentinelweb.cuer.app.ui.common.dialog.play.PlayDialog
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract
import uk.co.sentinelweb.cuer.app.ui.ytplayer.floating.FloatingPlayerServiceManager
import uk.co.sentinelweb.cuer.app.util.cast.listener.ChromecastYouTubePlayerContextHolder
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferencesWrapper
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

class PlayUseCase constructor(
    private val queue: QueueMediatorContract.Producer,
    private val ytCastContextHolder: ChromecastYouTubePlayerContextHolder,
    private val prefsWrapper: GeneralPreferencesWrapper,
    private val coroutines: CoroutineContextProvider,
    private val floatingService: FloatingPlayerServiceManager,
    private val playDialog: PlayDialog
) {

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
        if (queue.playlistId == itemDomain.playlistId?.toIdentifier(LOCAL)) {
            queue.onItemSelected(itemDomain, resetPosition = resetPos)
        } else {
            playDialog.showDialog(mapChangePlaylistAlert({
                val toIdentifier = itemDomain.playlistId!!.toIdentifier(LOCAL)

                prefsWrapper.putPair(
                    GeneralPreferences.CURRENT_PLAYLIST,
                    toIdentifier.toPairType<Long>()
                )
                coroutines.computationScope.launch {
                    queue.switchToPlaylist(toIdentifier)
                    queue.onItemSelected(itemDomain, forcePlay = true, resetPosition = resetPos)
                }
            }, {/*cancel*/ }
            ))
        }
    }

    private fun mapChangePlaylistAlert(confirm: () -> Unit, info: () -> Unit): AlertDialogModel =
        AlertDialogModel(
            R.string.playlist_change_dialog_title,
            R.string.playlist_change_dialog_message,
            AlertDialogModel.Button(R.string.ok, confirm),
            AlertDialogModel.Button(R.string.dialog_button_view_info, info)
        )

    fun setQueueItem(item: PlaylistItemDomain) {
        coroutines.computationScope.launch {
            val toIdentifier = item.playlistId?.toIdentifier(LOCAL)
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