package uk.co.sentinelweb.cuer.app.ui.share

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.exception.NoDefaultPlaylistException
import uk.co.sentinelweb.cuer.app.queue.QueueMediatorContract
import uk.co.sentinelweb.cuer.app.ui.share.scan.ScanContract
import uk.co.sentinelweb.cuer.app.util.cast.listener.ChromecastYouTubePlayerContextHolder
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences
import uk.co.sentinelweb.cuer.app.util.prefs.SharedPrefsWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.ToastWrapper
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.providers.TimeProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.ObjectTypeDomain.MEDIA
import uk.co.sentinelweb.cuer.domain.ObjectTypeDomain.PLAYLIST
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

class SharePresenter constructor(
    private val view: ShareContract.View,
    private val contextProvider: CoroutineContextProvider,
    private val toast: ToastWrapper,
    private val queue: QueueMediatorContract.Producer,
    private val state: ShareContract.State,
    private val ytContextHolder: ChromecastYouTubePlayerContextHolder,
    private val log: LogWrapper,
    private val mapper: ShareModelMapper,
    private val prefsWrapper: SharedPrefsWrapper<GeneralPreferences>,
    private val timeProvider: TimeProvider
) : ShareContract.Presenter {

    init {
        log.tag(this)
    }


    private fun mapDisplayModel() {
        (state.scanResult
            ?.let {
                mapper.mapShareModel(it, ::finish)
            }
            ?: mapper.mapEmptyState(::finish))// todo fail result
            .apply {
                state.model = this
                view.setData(this)
            }
    }

    override fun linkError(clipText: String?) {
        log.e("cannot add url : $clipText")
        clipText?.apply {
            view.warning("Cannot add : ${this.take(100)}")
            view.setData(mapper.mapEmptyState(::finish))
        } ?: run {
            view.warning("Nothing to add ...")
            view.setData(mapper.mapEmptyState(::finish))
        }
    }

    override fun scanResult(result: ScanContract.Result) {
        state.scanResult = result
        when (result.type) {
            MEDIA -> (result.result as MediaDomain).let {
                view.showMedia(PlaylistItemDomain(null, it, timeProvider.instant(), 0, false, null))
                mapDisplayModel()
            }
            PLAYLIST -> TODO()
        }
    }

    private fun finish(add: Boolean, play: Boolean, forward: Boolean) {
        state.viewModelScope.launch {
            try {
                if (add) {// fixme if playlist items are changed then they arent saved here
                    view.commitPlaylistItems()
                    queue.refreshQueue()
                }
                val isConnected = ytContextHolder.isConnected()
                val playlistItemList = view.getCommittedItems() as List<PlaylistItemDomain>?// todo change for playlist - will crash
//                val playlistItemList: List<PlaylistItemDomain>? = if (add)
//                    view.getPlaylistItems()
//                else {
//                    // todo fragment need to load playlist items for media here
//                    listOf()// todo existing state.playlistItems
//                }
                val size = playlistItemList?.size ?: 0
                val currentPlaylistId = prefsWrapper.getLong(GeneralPreferences.CURRENT_PLAYLIST_ID)
                val playlistItem: PlaylistItemDomain? = if (size == 1) {
                    playlistItemList?.get(0)
                } else if (size > 1) {
                    val indexOfFirst = playlistItemList?.indexOfFirst { it.playlistId == currentPlaylistId }
                    playlistItemList?.get(
                        indexOfFirst?.let { if (it > -1) it else 0 } ?: 0
                    )
                } else null

                if (forward) {
                    log.d("finish:play = $play, item = $playlistItem")
                    view.gotoMain(playlistItem, play)
                    view.exit()
                } else { // return play is hidden for not connected
                    playlistItem
                        ?.takeIf { play }
                        ?.takeIf { isConnected }
                        ?.let { pli ->
                            pli.playlistId?.let { itemPlaylistId ->
                                queue.playNow(itemPlaylistId, pli.id)
                            } ?: throw IllegalArgumentException("Item had no playlist")
                        }
                    view.exit()
                }
            } catch (t: Throwable) {
                when (t) {
                    is NoDefaultPlaylistException -> // todo make a dialog or just create the playlist
                        view.error("No default playlist - select a playlist to save the item")
                    else -> view.error(t.message ?: (t::class.java.simpleName + " error ... sorry"))
                }
            }
        }
    }

    override fun onStop() {
        state.jobs.forEach { it.cancel() }
    }

    private fun errorLoading(token: String) {
        "Couldn't get youtube info for $token".apply {
            log.e(this)
            view.warning(this)
        }
    }

}