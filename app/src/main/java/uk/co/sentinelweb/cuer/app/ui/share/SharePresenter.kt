package uk.co.sentinelweb.cuer.app.ui.share

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.db.repository.MediaDatabaseRepository
import uk.co.sentinelweb.cuer.app.queue.QueueMediatorContract
import uk.co.sentinelweb.cuer.app.util.cast.listener.ChromecastYouTubePlayerContextHolder
import uk.co.sentinelweb.cuer.app.util.share.scan.LinkScanner
import uk.co.sentinelweb.cuer.app.util.wrapper.ToastWrapper
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.net.youtube.YoutubeInteractor
import uk.co.sentinelweb.cuer.net.youtube.videos.YoutubePart.*

class SharePresenter constructor(
    private val view: ShareContract.View,
    private val repository: MediaDatabaseRepository,
    private val linkScanner: LinkScanner,
    private val contextProvider: CoroutineContextProvider,
    private val ytInteractor: YoutubeInteractor,
    private val toast: ToastWrapper,
    private val queue: QueueMediatorContract.Mediator,
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
                            loadMedia(it)
                            it.id?.apply { view.warning("Video already in queue ...") }
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

    private fun loadMedia(it: MediaDomain) {
        state.media = it
        view.setData(mapper.mapShareModel(it, ::finish))
    }

    private fun finish(add: Boolean, play: Boolean, forward: Boolean) {
        state.jobs.add(CoroutineScope(contextProvider.Main).launch {
            if (add) {
                view.commitPlaylistItems()
                queue.refreshQueue()

//                state.media
//                    ?.let { repository.save(it) }
//                    ?.takeIf { it.isSuccessful }
//                    ?.also { queue.refreshQueue() }
//                    ?.apply {
//                        data?.let {
//                            state.media = data
//                            view.setData(mapper.mapShareModel(it, ::finish))
//
//                        }
//
//                    }
            }
            val isConnected = ytContextHolder.isConnected()
            if (forward) {
                view.gotoMain(state.media, play)
                view.exit()
            } else { // return play is hidden for not connected
                play.takeIf { it }
                    ?.takeIf { isConnected }
                    ?.let { state.media }
                    ?.also {
                        queue.getItemFor(it.url)
                            ?.run { queue.onItemSelected(this) }
                            .run { view.exit() }

                    } ?: view.exit()
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