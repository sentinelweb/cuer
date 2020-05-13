package uk.co.sentinelweb.cuer.app.ui.share

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.db.repository.MediaDatabaseRepository
import uk.co.sentinelweb.cuer.app.queue.QueueMediatorContract
import uk.co.sentinelweb.cuer.app.util.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.ToastWrapper
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.net.youtube.YoutubeVideosInteractor
import uk.co.sentinelweb.cuer.net.youtube.videos.YoutubePart.*

class SharePresenter constructor(
    private val view: ShareContract.View,
    private val repository: MediaDatabaseRepository,
    private val linkScanner: LinkScanner,
    private val contextProvider: CoroutineContextProvider,
    private val ytInteractor: YoutubeVideosInteractor,
    private val toast: ToastWrapper,
    private val queue: QueueMediatorContract.Mediator,
    private val state: ShareState,
    private val log: LogWrapper

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
                        ?.also { loadMedia(it) }
                        ?: errorLoading(scannedMedia.url)
                })
            } ?: unableExit(uriString)
    }

    private suspend fun loadOrInfo(scannedMedia: MediaDomain): MediaDomain? = scannedMedia.let {
        repository.loadList(MediaDatabaseRepository.MediaIdFilter(scannedMedia.mediaId))
    }.takeIf { it.isSuccessful }
        ?.let { it.data?.firstOrNull() }
        ?: run {
            try {
                ytInteractor.videos(
                    ids = listOf(scannedMedia.mediaId),
                    parts = listOf(ID, SNIPPET, CONTENT_DETAILS)
                )
            } catch (e: Exception) {
                log.d("Couldn't get info for item: ${scannedMedia.url}")
                listOf<MediaDomain>()
            }
        }.firstOrNull()

    private fun loadMedia(it: MediaDomain) {
        state.media = it
        view.setData(it)
    }

    override fun onAddReturn() {
        state.jobs.add(CoroutineScope(contextProvider.Main).launch {
            state.media
                ?.also { repository.save(it) }
                .also { queue.refreshQueue() }
                .also { view.exit() }
        })
    }

    override fun onAddForward() {
        state.jobs.add(CoroutineScope(contextProvider.Main).launch {
            state.media
                ?.also { repository.save(it) }
                .also {
                    view.gotoMain(it)
                    view.exit()
                }
        })
    }

    override fun onPlayNow() {
        state.jobs.add(CoroutineScope(contextProvider.Main).launch {
            state.media
                ?.also { repository.save(it) }
                .also { queue.refreshQueue() }
                .also {
                    view.gotoMain(it, true)
                    view.exit()
                }
        })
    }

    override fun onReject() {
        view.exit()
    }

    private fun errorLoading(token: String) {
        view.exit()
        toast.show("Couldn't load item: $token")
        log.d("Couldn't load item: $token")
    }

    private fun unableExit(uri: String) {
        view.error("Unable to process link $uri")
        view.exit()
    }

}