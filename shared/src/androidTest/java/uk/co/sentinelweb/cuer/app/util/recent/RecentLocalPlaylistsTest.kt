package uk.co.sentinelweb.cuer.app.util.recent

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferences.RECENT_PLAYLISTS
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferencesWrapperImpl
import uk.co.sentinelweb.cuer.core.wrapper.SystemLogWrapper

class RecentLocalPlaylistsTest {

    private val realMultiPrefs = MultiPlatformPreferencesWrapperImpl()
    private val log = SystemLogWrapper()
//    private val mockLocalSearch = mockk<LocalSearchPlayistInteractor>(relaxed = true)

    private val sut = RecentLocalPlaylists(realMultiPrefs, log/*, mockLocalSearch*/) //, mockPrefs

    @Before
    fun setUp() {
        realMultiPrefs.remove(RECENT_PLAYLISTS)
        realMultiPrefs.lastAddedPlaylistId = null
        realMultiPrefs.pinnedPlaylistId = null
    }

    @Test
    fun getRecent() {
        sut.addRecentId(1)
        sut.addRecentId(2)
        sut.addRecentId(3)
        sut.addRecentId(4)

        assertEquals(listOf(4L, 3L, 2L, 1L), sut.getRecent().reversed())
    }

    @Test
    fun addRecent() {
    }

    @Test
    fun addRecentId() {
    }

    @Test
    fun buildRecentSelectionList() {
    }
}