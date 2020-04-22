package uk.co.sentinelweb.cuer.app.ui.share

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.db.repository.MediaDatabaseRepository
import uk.co.sentinelweb.cuer.app.util.provider.CoroutineContextProvider

class SharePresenter constructor(
    private val view: ShareContract.View,
    private val repository: MediaDatabaseRepository,
    private val linkScanner: LinkScanner,
    private val contextProvider: CoroutineContextProvider
) : ShareContract.Presenter {
    internal var jobs = mutableListOf<Job>()

    override fun fromShareUrl(uriString: String) {
        linkScanner
            .scan(uriString)
            ?.let {
                jobs.add(CoroutineScope(contextProvider.Main).launch {
                    repository.save(it)
                    repository.loadList(MediaDatabaseRepository.MediaIdFilter(it.mediaId))[0]
                    view.launchYoutubeVideo(it.mediaId)
                    view.exit()
                })
            } ?: unableExit(uriString)
    }

    private fun unableExit(uri: String) {
        view.error("Unable to process link $uri")
        view.exit()
    }

}