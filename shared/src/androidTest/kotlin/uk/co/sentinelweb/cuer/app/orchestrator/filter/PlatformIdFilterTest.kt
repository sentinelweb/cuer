package uk.co.sentinelweb.cuer.app.orchestrator.filter

import com.flextrade.jfixture.JFixture
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Operation.FULL
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlatformDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.tools.ext.build

class PlatformIdFilterTest {
    private val fixture = JFixture()

    private val sut: PlatformIdFilter = PlatformIdFilter()

    @Before
    fun setUp() {
    }

    @Test
    fun shouldMatchPlaylistPlatformId() {
        val targetId: String = fixture.build()
        val playlist: PlaylistDomain = fixture.build<PlaylistDomain>()
            .copy(
                platformId = targetId,
                platform = PlatformDomain.YOUTUBE
            )
        sut.targetPlatformId = targetId
        sut.targetDomainClass = PlaylistDomain::class

        assertTrue(sut.compareTo(Triple(FULL, LOCAL, playlist)))
    }

    @Test
    fun shouldNotMatchPlaylistPlatformId() {
        val targetId: String = fixture.build()
        val playlist: PlaylistDomain = fixture.build<PlaylistDomain>()
            .copy(
                platformId = "differentId",
                platform = PlatformDomain.YOUTUBE
            )
        sut.targetPlatformId = targetId
        sut.targetDomainClass = PlaylistDomain::class

        assertFalse(sut.compareTo(Triple(FULL, LOCAL, playlist)))
    }

    @Test
    fun shouldMatchPlaylistItemPlatformId() {
        val targetId: String = fixture.build()
        val playlist: PlaylistItemDomain = fixture.build<PlaylistItemDomain>().let {
            it.copy(
                media = it.media.copy(
                    platformId = targetId,
                    platform = PlatformDomain.YOUTUBE
                )
            )
        }
        sut.targetPlatformId = targetId
        sut.targetDomainClass = PlaylistItemDomain::class

        assertTrue(sut.compareTo(Triple(FULL, LOCAL, playlist)))
    }

    @Test
    fun shouldNotMatchPlaylistItemPlatformId() {
        val targetId: String = fixture.build()
        val playlist: PlaylistItemDomain = fixture.build<PlaylistItemDomain>().let {
            it.copy(
                media = it.media.copy(
                    platformId = "differentId",
                    platform = PlatformDomain.YOUTUBE
                )
            )
        }
        sut.targetPlatformId = targetId
        sut.targetDomainClass = PlaylistItemDomain::class

        assertFalse(sut.compareTo(Triple(FULL, LOCAL, playlist)))
    }

    @Test
    fun shouldMatchMediaPlatformId() {
        val targetId: String = fixture.build()
        val playlist: MediaDomain = fixture.build<MediaDomain>().copy(
            platformId = targetId,
            platform = PlatformDomain.YOUTUBE
        )

        sut.targetPlatformId = targetId
        sut.targetDomainClass = MediaDomain::class

        assertTrue(sut.compareTo(Triple(FULL, LOCAL, playlist)))
    }

    @Test
    fun shouldNotMatchMediaPlatformId() {
        val targetId: String = fixture.build()
        val playlist: MediaDomain = fixture.build<MediaDomain>().copy(
            platformId = "differentId",
            platform = PlatformDomain.YOUTUBE
        )

        sut.targetPlatformId = targetId
        sut.targetDomainClass = MediaDomain::class

        assertFalse(sut.compareTo(Triple(FULL, LOCAL, playlist)))
    }
}