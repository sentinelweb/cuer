package uk.co.sentinelweb.cuer.app.ui.playlist

import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.db.repository.MediaDatabaseRepository
import uk.co.sentinelweb.cuer.app.util.provider.CoroutineContextProvider
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.MediaDomain.MediaTypeDomain.VIDEO
import uk.co.sentinelweb.cuer.domain.MediaDomain.PlatformDomain.YOUTUBE
import uk.co.sentinelweb.cuer.ui.queue.dummy.Queue

class PlaylistPresenter(
    private val view: PlaylistContract.View,
    private val state: PlaylistState,
    private val repository: MediaDatabaseRepository,
    private val modelMapper: PlaylistModelMapper,
    private val contextProvider: CoroutineContextProvider
) : PlaylistContract.Presenter {

    internal var jobs = mutableListOf<Job>()

    override fun initialise() {
        initList()
    }

    override fun loadList() {
        jobs.add(contextProvider.MainScope.launch {
            repository
                .loadList(null)
                .map { modelMapper.map(it) }
                .also { view.setList(it) }
        })
    }

    private fun initList() {
        jobs.add(contextProvider.MainScope.launch {
            val count = repository.count()
            if (count == 0) {
                Queue.ITEMS
                    .map { map(it) }
                    .also { repository.save(it) }
                    .also { loadList() }
            }
        })
    }

    private fun map(it: Queue.QueueItem): MediaDomain {
        return MediaDomain(
            url = it.url,
            mediaId = it.getId(),
            title = it.title,
            platform = YOUTUBE,
            description = null,
            dateLastPlayed = null,
            duration = null,
            mediaType = VIDEO,
            id = null,
            positon = null
        )
    }

}