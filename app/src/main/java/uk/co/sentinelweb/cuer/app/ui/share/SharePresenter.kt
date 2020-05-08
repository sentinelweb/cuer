package uk.co.sentinelweb.cuer.app.ui.share

import com.roche.mdas.util.wrapper.ToastWrapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.db.repository.MediaDatabaseRepository
import uk.co.sentinelweb.cuer.app.queue.QueueMediatorContract
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
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
    private val state: ShareState
) : ShareContract.Presenter {

    override fun fromShareUrl(uriString: String) {
        linkScanner
            .scan(uriString)
            ?.let { scannedMedia ->
                state.jobs.add(CoroutineScope(contextProvider.Main).launch {
                    scannedMedia.let {
                        repository.loadList(MediaDatabaseRepository.MediaIdFilter(scannedMedia.mediaId))
                    }.takeIf { it.isEmpty() }
                        ?.run {
                            ytInteractor.videos(
                                ids = listOf(scannedMedia.mediaId),
                                parts = listOf(ID, SNIPPET, CONTENT_DETAILS)
                            )
                        }
                        ?.firstOrNull()
                        ?.also {
                            state.media = it
                            view.setData(it)
                        } ?: skipExists()
                })
            } ?: unableExit(uriString)
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

    private fun skipExists() {
        view.exit()
        toast.show("We have it already ...")
    }

    private fun unableExit(uri: String) {
        view.error("Unable to process link $uri")
        view.exit()
    }

}