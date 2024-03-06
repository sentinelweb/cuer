package uk.co.sentinelweb.cuer.app.util.recent

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferences.RECENT_PLAYLISTS
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferencesWrapperImpl
import uk.co.sentinelweb.cuer.core.wrapper.SystemLogWrapper
import uk.co.sentinelweb.cuer.tools.ext.guid

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

    // todo fix
    @Test
    @Ignore("broken after refactor")
    fun getRecent() {
        val ids = (1..4).map { guid() }
        sut.addRecentId(ids[0])
        sut.addRecentId(ids[1])
        sut.addRecentId(ids[2])
        sut.addRecentId(ids[3])

        assertEquals(ids.reversed(), sut.getRecent())
    }

//    @Test
//    fun addRecent() {
//    }

//    @Test
//    fun addRecentId() {
//    }

//    @Test
//    fun buildRecentSelectionList() {
//    }
}