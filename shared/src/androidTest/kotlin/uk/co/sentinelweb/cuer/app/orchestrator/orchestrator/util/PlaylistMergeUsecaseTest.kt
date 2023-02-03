package uk.co.sentinelweb.cuer.app.orchestrator.orchestrator.util

import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import uk.co.sentinelweb.cuer.app.orchestrator.PlaylistOrchestrator
import uk.co.sentinelweb.cuer.app.usecase.PlaylistMergeUsecase
import uk.co.sentinelweb.cuer.core.wrapper.SystemLogWrapper
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistTypeDomain.*
import uk.co.sentinelweb.cuer.tools.ext.id

class PlaylistMergeUsecaseTest {

    @MockK
    lateinit var mockPlaylistOrchestrator: PlaylistOrchestrator

    private val log = SystemLogWrapper()

    private lateinit var sut: PlaylistMergeUsecase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        sut = PlaylistMergeUsecase(playlistOrchestrator = mockPlaylistOrchestrator, log = log)
    }

    @Test
    fun checkMerge_user_platform() {
        val delete = PlaylistDomain(id(), "user", type = USER)
        val receive = PlaylistDomain(id(), "platform", type = PLATFORM, platformId = "xxxx")

        assertTrue(sut.checkMerge(delete, receive))
    }

    @Test
    fun checkMerge_user_platform_no_platform_id() {
        val delete = PlaylistDomain(id(), "user", type = USER)
        val receive = PlaylistDomain(id(), "platform", type = PLATFORM, platformId = null)

        assertFalse(sut.checkMerge(delete, receive))
    }

    @Test
    fun checkMerge_both_platform() {
        val delete = PlaylistDomain(id(), "platform1", type = PLATFORM, platformId = "xxxxx")
        val receive = PlaylistDomain(id(), "platform2", type = PLATFORM, platformId = "xxxx")

        assertFalse(sut.checkMerge(delete, receive))
    }

    @Test
    fun checkMerge_one_app() {
        val delete = PlaylistDomain(id(), "app", type = APP)
        val receive = PlaylistDomain(id(), "platform", type = PLATFORM, platformId = "xxxx")

        assertFalse(sut.checkMerge(delete, receive))
    }

    @Test
    fun checkMerge_both_app() {
        val delete = PlaylistDomain(id(), "app1", type = APP)
        val receive = PlaylistDomain(id(), "app2", type = APP)

        assertFalse(sut.checkMerge(delete, receive))
    }

    @Test
    fun checkMerge_noid() {
        val delete = PlaylistDomain(null, "user1", type = USER)
        val receive = PlaylistDomain(id(), "user2", type = USER)

        assertFalse(sut.checkMerge(delete, receive))
    }

    @Test
    fun checkMerge_both_user() {
        val delete = PlaylistDomain(id(), "user1", type = USER)
        val receive = PlaylistDomain(id(), "user2", type = USER)

        assertTrue(sut.checkMerge(delete, receive))
    }

    @Test
    fun checkMerge_same_id() {
        val sameId = id()
        val delete = PlaylistDomain(sameId, "user1", type = USER)
        val receive = PlaylistDomain(sameId, "user1", type = USER)

        assertFalse(sut.checkMerge(delete, receive))
    }

    @Test
    fun merge() {
    }
}