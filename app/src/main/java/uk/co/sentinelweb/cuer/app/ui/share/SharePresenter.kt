package uk.co.sentinelweb.cuer.app.ui.share

import com.roche.mdas.util.wrapper.ToastWrapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.db.repository.MediaDatabaseRepository
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.net.youtube.YoutubeVideosInteractor
import uk.co.sentinelweb.cuer.net.youtube.videos.YoutubePart.*

class SharePresenter constructor(
    private val view: ShareContract.View,
    private val repository: MediaDatabaseRepository,
    private val linkScanner: LinkScanner,
    private val contextProvider: CoroutineContextProvider,
    private val ytInteractor: YoutubeVideosInteractor,
    private val toast: ToastWrapper
) : ShareContract.Presenter {
    internal var jobs = mutableListOf<Job>()

    override fun fromShareUrl(uriString: String) {
        linkScanner
            .scan(uriString)
            ?.let { scannedMedia ->
                jobs.add(CoroutineScope(contextProvider.Main).launch {
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
                        ?.also { repository.save(it) }
                        ?.also {
                            view.gotoMain(it.mediaId)
                            view.exit()
                        } ?: skipExists()
                })
            } ?: unableExit(uriString)
    }

    private fun skipExists() {
        view.exit()
        toast.showToast("We have it already ...")
    }

    private fun unableExit(uri: String) {
        view.error("Unable to process link $uri")
        view.exit()
    }

}