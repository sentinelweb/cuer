package uk.co.sentinelweb.cuer.app.ui.playlist

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
import uk.co.sentinelweb.cuer.domain.GUID
import uk.co.sentinelweb.cuer.domain.creator.GuidCreator
import uk.co.sentinelweb.cuer.domain.ext.currentItem
import uk.co.sentinelweb.cuer.tools.ext.generatePlaylist
import uk.co.sentinelweb.cuer.tools.ext.kotlinFixtureDefaultConfig

class PlaylistMviModelMapperTest {
    private val fixture = kotlinFixtureDefaultConfig

    private val itemModelMapper: PlaylistMviItemModelMapper = mockk(relaxed = true)
    private val iconMapper: IconMapper = mockk(relaxed = true)
    private val strings: StringDecoder = mockk(relaxed = true)
    private val appPlaylistInteractors: Map<OrchestratorContract.Identifier<GUID>, AppPlaylistInteractor> = mapOf()
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
        assertNull(actual.playingItemId)
        assertNull(actual.identifier)
        assertFalse(actual.isCards)
        assertEquals("", actual.header.title)
    }

    @Test
    fun mapPlaylist() {
        val initial = generatePlaylist(fixture).let {
            it.copy(id = it.id ?: GuidCreator().create().toIdentifier(fixture()), items = it.items.filter { it.id != null })
        }

        val identifier = initial.id!!
        val state = PlaylistMviContract.MviStore.State(
            playlist = initial,
            playlistIdentifier = identifier,
            itemsIdMap = initial.items.associate { it.id!! to it }.toMutableMap(),
            itemsIdMapReversed = initial.items.associate { it to it.id!! }.toMutableMap(),
        )
        every {
            itemModelMapper.mapItem(any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns fixture()

        val actual = sut.map(state)

        assertEquals(identifier, actual.identifier)
        assertEquals(initial.items.size, actual.items?.size)
        assertEquals(initial.currentItem()?.id, actual.playingItemId)
    }
}
