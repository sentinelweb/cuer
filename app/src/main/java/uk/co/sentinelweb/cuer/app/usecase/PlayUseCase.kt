package uk.co.sentinelweb.cuer.app.usecase

import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.orchestrator.toIdentifier
import uk.co.sentinelweb.cuer.app.orchestrator.toPairType
import uk.co.sentinelweb.cuer.app.queue.QueueMediatorContract
import uk.co.sentinelweb.cuer.app.ui.common.dialog.AlertDialogCreator
import uk.co.sentinelweb.cuer.app.ui.common.dialog.AlertDialogModel
import uk.co.sentinelweb.cuer.app.ui.common.dialog.play.PlayDialog
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
    private val alertDialogCreator: AlertDialogCreator,
    private val playDialog: PlayDialog
) {

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
            // todo show dialog
//            navigationMapper.navigate(
//                NavigationModel(
//                    NavigationModel.Target.LOCAL_PLAYER,
//                    mapOf(NavigationModel.Param.PLAYLIST_ITEM to itemDomain)
//                )
//            )
        }
    }

    private fun playItem(itemDomain: PlaylistItemDomain, resetPos: Boolean) {
        if (queue.playlistId == itemDomain.playlistId?.toIdentifier(OrchestratorContract.Source.LOCAL)) {
            queue.onItemSelected(itemDomain, resetPosition = resetPos)
        } else {
            alertDialogCreator.create(mapChangePlaylistAlert({
                val toIdentifier =
                    itemDomain.playlistId!!.toIdentifier(OrchestratorContract.Source.LOCAL)

                prefsWrapper.putPair(
                    GeneralPreferences.CURRENT_PLAYLIST,
                    toIdentifier.toPairType<Long>()
                )
                coroutines.computationScope.launch {
                    queue.switchToPlaylist(toIdentifier)
                    queue.onItemSelected(itemDomain, forcePlay = true, resetPosition = resetPos)
                }
            }, {// info
                //view.showItemDescription(modelId, itemDomain, state.playlistIdentifier.source)
            }))
        }
    }

    fun mapChangePlaylistAlert(confirm: () -> Unit, info: () -> Unit): AlertDialogModel =
        AlertDialogModel(
            R.string.playlist_change_dialog_title,
            R.string.playlist_change_dialog_message,
            AlertDialogModel.Button(R.string.ok, confirm),
            AlertDialogModel.Button(R.string.dialog_button_view_info, info)
        )
}