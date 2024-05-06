package uk.co.sentinelweb.cuer.app.util.share.scan

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import uk.co.sentinelweb.cuer.app.util.wrapper.log.AndroidLogWrapper
import uk.co.sentinelweb.cuer.domain.*
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class AndroidLinkScannerTest {
    val sut = AndroidLinkScanner(
        log = AndroidLogWrapper(BuildConfigDomain(true, true, 50, "version", "device", NodeDomain.DeviceType.ANDROID)),
        mappers = urlMediaMappers
    )

    @Test
    fun shorts_url() {
        val testLink = "https://www.youtube.com/shorts/lq9hzALa4Po?feature=share"

        val actual = sut.scan(testLink)
        val expectedMedia = MediaDomain.createYoutube(testLink, "lq9hzALa4Po")
        assertNotNull(actual)
        assertEquals(actual?.first, ObjectTypeDomain.MEDIA)
        assertEquals(actual?.second, expectedMedia)
    }

    @Test
    fun shorts_url_with_c() {
        val testLink = "https://youtube.com/shorts/WsciBZQACk4?feature=share"

        val actual = sut.scan(testLink)
        val expectedMedia = MediaDomain.createYoutube(testLink, "WsciBZQACk4")
        assertNotNull(actual)
        assertEquals(actual?.first, ObjectTypeDomain.MEDIA)
        assertEquals(actual?.second, expectedMedia)
    }

    @Test
    fun channel_url_with_c() {
        val testLink = "https://www.youtube.com/c/MattGreenComedy"

        val actual = sut.scan(testLink)
        val expectedChannel = ChannelDomain.createYoutubeCustomUrl("/c/MattGreenComedy")
        assertNotNull(actual)
        assertEquals(actual?.first, ObjectTypeDomain.CHANNEL)
        assertEquals(actual?.second, expectedChannel)
        val actualChannel = actual!!.second as ChannelDomain
        assertEquals(actualChannel.platformId, NO_PLATFORM_ID)
        assertEquals(actualChannel.customUrl, "MattGreenComedy")
    }

    @Test
    fun channel_url_with_id() {
        val testLink = "https://www.youtube.com/channel/UCM191aISRy5AQ51wCXOGiEg"

        val actual = sut.scan(testLink)
        val expectedChannel = ChannelDomain.createYoutube("/channel/UCM191aISRy5AQ51wCXOGiEg")
        assertNotNull(actual)
        assertEquals(actual?.first, ObjectTypeDomain.CHANNEL)
        assertEquals(actual?.second, expectedChannel)
        val actualChannel = actual!!.second as ChannelDomain
        assertEquals(actualChannel.platformId, "UCM191aISRy5AQ51wCXOGiEg")
        assertNull(actualChannel.customUrl)
    }

    @Test
    fun media_watch_url() {
        val testLink = "https://www.youtube.com/watch?v=JqsQ_JjiFmg"

        val actual = sut.scan(testLink)
        val expectedMedia = MediaDomain.createYoutube(testLink, "JqsQ_JjiFmg")
        assertNotNull(actual)
        assertEquals(actual?.first, ObjectTypeDomain.MEDIA)
        assertEquals(actual?.second, expectedMedia)
        val actualMedia = actual!!.second as MediaDomain
        assertEquals(actualMedia.platformId, "JqsQ_JjiFmg")
    }

    @Test
    fun media_short_url() {
        val testLink = "https://youtu.be/JqsQ_JjiFmg"

        val actual = sut.scan(testLink)
        val expectedMedia = MediaDomain.createYoutube(testLink, "JqsQ_JjiFmg")
        assertNotNull(actual)
        assertEquals(actual?.first, ObjectTypeDomain.MEDIA)
        assertEquals(actual?.second, expectedMedia)
        val actualMedia = actual!!.second as MediaDomain
        assertEquals(actualMedia.platformId, "JqsQ_JjiFmg")
    }

    @Test
    fun playlist_url() {
        val testLink =
            "https://www.youtube.com/playlist?list=PLmmblQQ1XpT_qMQYyTERHsJ_KZqOIAoEe"

        val actual = sut.scan(testLink)
        val expectedPlaylist =
            PlaylistDomain.createYoutube(testLink, "PLmmblQQ1XpT_qMQYyTERHsJ_KZqOIAoEe")
        assertNotNull(actual)
        assertEquals(actual?.first, ObjectTypeDomain.PLAYLIST)
        assertEquals(actual?.second, expectedPlaylist)
        val actualPlaylist = actual!!.second as PlaylistDomain
        assertEquals(actualPlaylist.platformId, "PLmmblQQ1XpT_qMQYyTERHsJ_KZqOIAoEe")
    }

    @Test
    fun live_url() {
        val testLink =
            "https://www.youtube.com/live/nBkfPrWj3xQ?feature=share"

        val actual = sut.scan(testLink)
        val expectedMedia = MediaDomain.createYoutube(testLink, "nBkfPrWj3xQ")
        assertNotNull(actual)
        assertEquals(actual?.first, ObjectTypeDomain.MEDIA)
        assertEquals(actual?.second, expectedMedia)
        val actualMedia = actual!!.second as MediaDomain
        assertEquals(actualMedia.platformId, "nBkfPrWj3xQ")
    }

    @Test
    fun google_yt_url() {
        val testLink =
            "https://www.google.com/url?sa=t&source=web&rct=j&url=https://m.youtube.com/watch%3Fv%3D88YCkY8U2NU&ved=2ahUKEwiuh5DxpYH7AhWJx4UKHVrKB1IQwqsBegQIdhAF&usg=AOvVaw0faoqETwWJ6C2Y_kMcucLW"

        val actual = sut.scan(testLink)
        val expectedMedia = MediaDomain.createYoutube("https://m.youtube.com/watch?v=88YCkY8U2NU", "88YCkY8U2NU")
        assertNotNull(actual)
        assertEquals(actual?.first, ObjectTypeDomain.MEDIA)
        assertEquals(actual?.second, expectedMedia)
        val actualMedia = actual!!.second as MediaDomain
        assertEquals(actualMedia.platformId, "88YCkY8U2NU")
    }
}
