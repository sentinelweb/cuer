package uk.co.sentinelweb.cuer.app.db.init

import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.db.repository.MediaDatabaseRepository
import uk.co.sentinelweb.cuer.app.db.repository.PlaylistDatabaseRepository
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.providers.TimeProvider
import uk.co.sentinelweb.cuer.domain.*
import uk.co.sentinelweb.cuer.net.youtube.YoutubeInteractor

class DatabaseInitializer constructor(
    private val ytInteractor: YoutubeInteractor,
    private val contextProvider: CoroutineContextProvider,
    private val playlistRepository: PlaylistDatabaseRepository,
    private val mediaRepository: MediaDatabaseRepository,
    private val timeProvider: TimeProvider
) {

    fun initDatabase() {
        contextProvider.IOScope.launch {
            mediaRepository.count()
                .takeIf { it.isSuccessful && it.data == 0 }
                ?.let { initPlaylists() }
                ?.let { it[0] to ITEMS }
                ?.let { (playlist, items) -> playlist to items.map { mapQueueToMedia(it) } }
                ?.let { (playlist, medias) ->
                    playlist to ytInteractor.videos(medias.map { it.platformId })
                }
                ?.takeIf { (_, result) -> result.isSuccessful }
                ?.let { (playlist, result) ->
                    playlist to result.data?.let { mediaRepository.save(it) }
                }
                ?.let { (playlist, result) -> makePlaylistItems(playlist, result?.data!!) }
        }
    }

    private fun makePlaylistItems(
        playlist: PlaylistDomain,
        medias: List<MediaDomain>
    ): List<PlaylistItemDomain> = medias.mapIndexed { i, media ->
        PlaylistItemDomain(
            media = media,
            dateAdded = timeProvider.instant(),
            order = i * 1000L,
            archived = false,
            playlistId = playlist.id
        )
    }

    suspend fun initPlaylists(): List<PlaylistDomain> =
        playlistRepository.count()
            .takeIf { it.isSuccessful && it.data == 0 }
            ?.let {
                listOf(
                    DEFAULT_PLAYLIST,
                    PlaylistDomain(
                        title = "Music",
                        image = ImageDomain(url = "gs://cuer-275020.appspot.com/playlist_header/pexels-cottonbro-4088009-600.jpg")
                    ),
                    PlaylistDomain(
                        title = "News",
                        image = ImageDomain(url = "gs://cuer-275020.appspot.com/playlist_header/pexels-brotin-biswas-518543-600.jpg")
                    ),
                    PlaylistDomain(
                        title = "Philosophy",
                        image = ImageDomain(url = "gs://cuer-275020.appspot.com/playlist_header/pexels-matheus-bertelli-2674271-600.jpg")
                    ),
                    PlaylistDomain(
                        title = "Comedy",
                        image = ImageDomain(url = "gs://cuer-275020.appspot.com/playlist_header/pexels-tim-mossholder-1115680-600.jpg")
                    )
                )
            }
            ?.let { playlistRepository.save(it) }
            ?.takeIf { it.isSuccessful }?.data
            ?: playlistRepository.loadList().data!!

    private fun mapQueueToMedia(it: QueueItem) = MediaDomain(
        url = it.url,
        platformId = it.getId(),
        title = it.title,
        platform = PlatformDomain.YOUTUBE,
        description = null,
        dateLastPlayed = null,
        duration = null,
        mediaType = MediaDomain.MediaTypeDomain.VIDEO,
        id = null,
        positon = null,
        channelData = ChannelDomain(// todo add real data
            platformId = null,
            platform = PlatformDomain.YOUTUBE
        )
    )

    /**
     * A dummy item representing a piece of content.
     */
    data class QueueItem(
        val url: String,
        val title: String
    ) {
        override fun toString(): String = "$title - $url"
        fun getId() = url.substring(url.indexOf("?v=") + 3)
    }

    /**
     * An array of sample (dummy) items.
     */
    val ITEMS: List<QueueItem> = listOf(
        QueueItem(
            "https://www.youtube.com/watch?v=c2_t3M_vSsg",
            "Responding to a Pandemic: The Myth of Sisyphus"
        ),
        QueueItem(
            "https://www.youtube.com/watch?v=6a2dLVx8THA",
            "Animating Poststructuralism"
        ),
        QueueItem(
            "https://www.youtube.com/watch?v=CkHooEp3vRE",
            "Masters Of Money | Part 1 | John Maynard Keynes"
        ),
        QueueItem(
            "https://www.youtube.com/watch?v=EIYqTj402PE",
            "Masters Of Money | Part 2 | Friedrich Hayek"
        ),
        QueueItem(
            "https://www.youtube.com/watch?v=oaIpYo3Z5lc",
            "Masters Of Money | Part 3 | Karl Marx"
        ),
        QueueItem(
            "https://www.youtube.com/watch?v=OFdgR8Zt084",
            "Order! High Voltage - John Bercow x Electric Six"
        ),
        QueueItem(
            "https://www.youtube.com/watch?v=vMiF9Bv-72s",
            "Adorno and Horkheimer: Dialectic of Enlightenment - Part I"
        ),
        QueueItem(
            "https://https://www.youtube.com/watch?v=AXyr4Zasdkg",
            "Foucault: Biopower, Governmentality, and the Subject"
        ),
        QueueItem(
            "https://www.youtube.com/watch?v=GNGvqjwich0&t=73s",
            "The Marketplace of Ideas: A Critique"
        ),
        QueueItem(
            "https://www.youtube.com/watch?v=52nqjrCs57s",
            "Why You Can't FOCUS - And How To Fix That"
        )
    )

    companion object {
        val DEFAULT_PLAYLIST = PlaylistDomain(
            title = "Default",
            image = ImageDomain(url = "gs://cuer-275020.appspot.com/playlist_header/pexels-freestocksorg-34407-600.jpg"),
            default = true
        )
    }
}