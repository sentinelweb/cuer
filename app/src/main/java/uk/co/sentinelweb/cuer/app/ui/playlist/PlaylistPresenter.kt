package uk.co.sentinelweb.cuer.app.ui.playlist

import kotlinx.coroutines.*
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

        Queue.ITEMS
            .map { MediaDomain(
                url = it.url,
                mediaId =  it.getId(),
                title = it.title,
                platform = YOUTUBE,
                description = null,
                dateLastPlayed = null,
                duration = null,
                mediaType = VIDEO,
                id = null,
                positon = null
            )}.also {
                saveList(it)
            }
    }

    private fun saveList(it: List<MediaDomain>) {
        // TODO find the better way to do this
        jobs.add(CoroutineScope(contextProvider.Main).launch {
            async(contextProvider.IO) { repository.save(it) }.await()
        })
    }


}