package uk.co.sentinelweb.cuer.app.db.init

import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.db.init.DatabaseInitializer.Companion.DEFAULT_PLAYLIST_TEMPLATE
import uk.co.sentinelweb.cuer.app.db.repository.MediaDatabaseRepository
import uk.co.sentinelweb.cuer.app.db.repository.PlaylistDatabaseRepository
import uk.co.sentinelweb.cuer.app.db.repository.PlaylistItemDatabaseRepository
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Filter.AllFilter
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.providers.TimeProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.*
import uk.co.sentinelweb.cuer.net.youtube.YoutubeInteractor

class MemoryDatabaseInitializer constructor(
    private val ytInteractor: YoutubeInteractor,
    private val contextProvider: CoroutineContextProvider,
    private val playlistRepository: PlaylistDatabaseRepository,
    private val playlistItemRepository: PlaylistItemDatabaseRepository,
    private val mediaRepository: MediaDatabaseRepository,
    private val timeProvider: TimeProvider,
    private val log: LogWrapper,
) : DatabaseInitializer {
    init {
        log.tag(this)
    }

    override fun isInitialized(): Boolean = true

    override fun initDatabase() {
        contextProvider.ioScope.launch {
            (mediaRepository.count(AllFilter)
                .takeIf { it.isSuccessful && it.data == 0 }
                ?: let { return@launch })
                .let { initPlaylists() }
                .let { it[0] to ITEMS }
                .let { (playlist, items) -> playlist to items.map { mapQueueToMedia(it) } }
                .let { (playlist, medias) ->
                    playlist to ytInteractor.videos(medias.map { it.platformId })
                }
                .takeIf { (_, result) -> result.isSuccessful }
                ?.let { (playlist, result) ->
                    playlist to result.data?.let { mediaRepository.save(it, flat = false, emit = false) }
                }
                ?.let { (playlist, result) -> makePlaylistItems(playlist, result?.data!!) }
                ?.let {
                    playlistItemRepository.save(it, emit = true, flat = false)
                }
                ?.takeIf { result -> result.isSuccessful }
                ?: throw IllegalStateException("failed to init database")
        }
    }

    override fun addListener(listener: (Boolean) -> Unit) {
        TODO("Not yet implemented")
    }

    private fun makePlaylistItems(
        playlist: PlaylistDomain,
        medias: List<MediaDomain>,
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
        playlistRepository.count(AllFilter)
            .takeIf { it.isSuccessful && it.data == 0 }
            ?.let {
                listOf(
                    DEFAULT_PLAYLIST_TEMPLATE,
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
                    ),
                    PlaylistDomain(
                        title = "Meditation",
                        image = ImageDomain(url = "gs://cuer-275020.appspot.com/playlist_header/pexels-emily-hopper-1359000-600.jpg")
                    )
                )
            }
            ?.let { playlistRepository.save(it, flat = false, emit = false) }
            ?.takeIf { it.isSuccessful }?.data
            ?: playlistRepository.loadList(AllFilter, flat = false).data!!

    private fun mapQueueToMedia(it: DefaultItem) = MediaDomain(
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
        channelData = ChannelDomain(
            platformId = null,
            platform = PlatformDomain.YOUTUBE
        )
    )

    data class DefaultItem(
        val url: String,
        val title: String,
    ) {
        override fun toString(): String = "$title - $url"
        fun getId() = url.substring(url.indexOf("?v=") + 3)
    }

    val ITEMS: List<DefaultItem> = listOf(
        DefaultItem(
            "https://www.youtube.com/watch?v=c2_t3M_vSsg",
            "Responding to a Pandemic: The Myth of Sisyphus"
        ),
        DefaultItem(
            "https://www.youtube.com/watch?v=6a2dLVx8THA",
            "Animating Poststructuralism"
        ),
        DefaultItem(
            "https://www.youtube.com/watch?v=CkHooEp3vRE",
            "Masters Of Money | Part 1 | John Maynard Keynes"
        ),
        DefaultItem(
            "https://www.youtube.com/watch?v=EIYqTj402PE",
            "Masters Of Money | Part 2 | Friedrich Hayek"
        ),
        DefaultItem(
            "https://www.youtube.com/watch?v=oaIpYo3Z5lc",
            "Masters Of Money | Part 3 | Karl Marx"
        ),
        DefaultItem(
            "https://www.youtube.com/watch?v=OFdgR8Zt084",
            "Order! High Voltage - John Bercow x Electric Six"
        ),
        DefaultItem(
            "https://www.youtube.com/watch?v=vMiF9Bv-72s",
            "Adorno and Horkheimer: Dialectic of Enlightenment - Part I"
        ),
        DefaultItem(
            "https://https://www.youtube.com/watch?v=AXyr4Zasdkg",
            "Foucault: Biopower, Governmentality, and the Subject"
        ),
        DefaultItem(
            "https://www.youtube.com/watch?v=GNGvqjwich0&t=73s",
            "The Marketplace of Ideas: A Critique"
        ),
        DefaultItem(
            "https://www.youtube.com/watch?v=52nqjrCs57s",
            "Why You Can't FOCUS - And How To Fix That"
        )
    )
}