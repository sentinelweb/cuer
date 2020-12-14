package uk.co.sentinelweb.cuer.app.ui.share

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.db.repository.MediaDatabaseRepository
import uk.co.sentinelweb.cuer.app.db.repository.PlaylistDatabaseRepository
import uk.co.sentinelweb.cuer.app.exception.NoDefaultPlaylistException
import uk.co.sentinelweb.cuer.app.queue.QueueMediatorContract
import uk.co.sentinelweb.cuer.app.util.cast.listener.ChromecastYouTubePlayerContextHolder
import uk.co.sentinelweb.cuer.app.util.share.scan.LinkScanner
import uk.co.sentinelweb.cuer.app.util.wrapper.ToastWrapper
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.net.youtube.YoutubeInteractor
import uk.co.sentinelweb.cuer.net.youtube.videos.YoutubePart.*

class SharePresenter constructor(
    private val view: ShareContract.View,
    private val repository: MediaDatabaseRepository,
    private val playlistRepository: PlaylistDatabaseRepository,
    private val linkScanner: LinkScanner,
    private val contextProvider: CoroutineContextProvider,
    private val ytInteractor: YoutubeInteractor,
    private val toast: ToastWrapper,
    private val queue: QueueMediatorContract.Producer,
    private val state: ShareState,
    private val ytContextHolder: ChromecastYouTubePlayerContextHolder,
    private val log: LogWrapper,
    private val mapper: ShareModelMapper
) : ShareContract.Presenter {

    init {
        log.tag = "SharePresenter"
    }

    override fun fromShareUrl(uriString: String) {
        linkScanner
            .scan(uriString)
            ?.let { scannedMedia ->
                state.jobs.add(CoroutineScope(contextProvider.Main).launch {
                    loadOrInfo(scannedMedia)
                        ?.also {
                            state.media = it
                            if (it.id != null) {
                                state.playlistItems =
                                    playlistRepository.loadPlaylistItems(PlaylistDatabaseRepository.MediaIdListFilter(listOf(it.id!!)))
                                        .takeIf { it.isSuccessful }
                                        ?.data
                            }
                            mapDisplayModel()
                            if (!(state.model?.isNewVideo ?: true)) {
                                view.warning("Video already in queue ...")
                            }
                        }
                        ?: errorLoading(scannedMedia.url)
                })
            } ?: linkError(uriString)
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

    private suspend fun loadOrInfo(scannedMedia: MediaDomain): MediaDomain? =
        scannedMedia.let {
            repository.loadList(MediaDatabaseRepository.MediaIdFilter(scannedMedia.platformId))
        }.takeIf { it.isSuccessful }
            ?.let { it.data?.firstOrNull() }
            ?: run {
                ytInteractor.videos(
                    ids = listOf(scannedMedia.platformId),
                    parts = listOf(ID, SNIPPET, CONTENT_DETAILS)
                ).takeIf { it.isSuccessful }
                    ?.let {
                        it.data?.firstOrNull()
                    }
            }

    private fun mapDisplayModel() {
        state.model = mapper.mapShareModel(state.media, state.playlistItems, ::finish).apply {
            view.setData(this)
        }
    }

    private fun finish(add: Boolean, play: Boolean, forward: Boolean) {
        state.jobs.add(CoroutineScope(contextProvider.Main).launch {
            try {
                if (add) {
                    view.commitPlaylistItems()
                    queue.refreshQueue()
                }
                //val isConnected = ytContextHolder.isConnected()
                if (forward) {
                    val media: PlaylistItemDomain? = if (add)
                        view.getPlaylistItems()[0]
                    else {
                        state.playlistItems?.get(0)

                    }
                    view.gotoMain(media, play)
                    view.exit()
                } else { // return play is hidden for not connected
                    // todo fix this when queue built
//                    play.takeIf { it }
//                        ?.takeIf { isConnected }
//                        ?.let { state.media }
//                        ?.also {
//                            queue.getItemFor(it.url)
//                                ?.run { queue.onItemSelected(this) }
//                                .run { view.exit() }
//                        } ?: view.exit()
                }
            } catch (t: Throwable) {
                when (t) {
                    is NoDefaultPlaylistException -> // todo make a dialog or just create the playlist
                        view.error("No default playlist - select a playlist to save the item")
                    else -> view.error(t.message ?: (t::class.java.simpleName + " error ... sorry"))
                }
            }
        })
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