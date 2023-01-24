package uk.co.sentinelweb.cuer.app.ui.playlist

import com.appmattus.kotlinfixture.decorator.nullability.NeverNullStrategy
import com.appmattus.kotlinfixture.decorator.nullability.nullabilityStrategy
import com.appmattus.kotlinfixture.kotlinFixture
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.orchestrator.memory.interactor.AppPlaylistInteractor
import uk.co.sentinelweb.cuer.app.orchestrator.toIdentifier
import uk.co.sentinelweb.cuer.app.ui.common.mapper.IconMapper
import uk.co.sentinelweb.cuer.app.ui.common.resources.StringDecoder
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferencesWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.PlaylistDomain

class PlaylistMviModelMapperTest {
    private val fixture = kotlinFixture { nullabilityStrategy(NeverNullStrategy) }

    private val itemModelMapper: PlaylistMviItemModelMapper = mockk(relaxed = true)
    private val iconMapper: IconMapper = mockk(relaxed = true)
    private val strings: StringDecoder = mockk(relaxed = true)
    private val appPlaylistInteractors: Map<Long, AppPlaylistInteractor> = mapOf()
    private val util: PlaylistMviUtil = mockk(relaxed = true)
    private val multiPlatformPreferences: MultiPlatformPreferencesWrapper = mockk(relaxed = true)
    private val log: LogWrapper = mockk(relaxed = true)

    private lateinit var sut: PlaylistMviModelMapper

    @Before
    fun setUp() {
        sut = PlaylistMviModelMapper(
            itemModelMapper,
            iconMapper,
            strings,
            appPlaylistInteractors,
            util,
            multiPlatformPreferences,
            log
        )
    }

    @Test
    fun mapEmpty() {
        val state = PlaylistMviContract.MviStore.State(playlist = null)

        val actual = sut.map(state)

        assertNull(actual.items)
        assertNull(actual.playingIndex)
        assertNull(actual.identifier)
        assertFalse(actual.isCards)
        assertEquals("", actual.header.title)
    }

    @Test
    fun mapPlaylist() {
        val playlist = fixture<PlaylistDomain>().let {
            it.copy(id = it.id ?: 3L, items = it.items.filter { it.id != null })
        }

        val identifier = playlist.id!!.toIdentifier(OrchestratorContract.Source.LOCAL)
        val state = PlaylistMviContract.MviStore.State(
            playlist = playlist,
            playlistIdentifier = identifier,
            itemsIdMap = playlist.items.associate { it.id!! to it }.toMutableMap()
        )
        every {
            itemModelMapper.mapItem(
                any(), any(), any(), any(), any(), any(), any(), any(), any()
            )
        } returns fixture()

        val actual = sut.map(state)

        assertEquals(identifier, actual.identifier)
        assertEquals(playlist.items.size, actual.items?.size)
        assertEquals(playlist.currentIndex, actual.playingIndex)
    }
}
