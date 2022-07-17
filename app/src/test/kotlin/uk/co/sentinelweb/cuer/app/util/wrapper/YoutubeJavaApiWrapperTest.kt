package uk.co.sentinelweb.cuer.app.util.wrapper

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import com.flextrade.jfixture.JFixture
import com.google.android.youtube.player.YouTubeIntents
import io.mockk.*
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import uk.co.sentinelweb.cuer.app.util.share.scan.LinkScanner
import uk.co.sentinelweb.cuer.domain.MediaDomain

// todo make another test with robolectric activity spy
class YoutubeJavaApiWrapperTest {

    private var mockActivity: AppCompatActivity = mockk(relaxUnitFun = true)
    private var mockLinkScanner: LinkScanner = mockk(relaxUnitFun = true)
    private var mockIntent: Intent = mockk(relaxUnitFun = true)
    private var mockUri: Uri = mockk(relaxUnitFun = true)
    private val fixture: JFixture = JFixture()
    private val fixtMedia: MediaDomain = fixture.create(MediaDomain::class.java)

    private var sut: YoutubeJavaApiWrapper = spyk(YoutubeJavaApiWrapper(mockActivity, mockLinkScanner))

    @Before
    fun setUp() {
        mockkStatic("com.google.android.youtube.player.YouTubeIntents")
        mockkStatic("android.net.Uri")
        //mockkStatic("com.google.android.youtube.player.YouTubeApiServiceUtil")
    }

    @Test
    @Ignore("Mock (fixme) isn't working")
    fun launchChannel() {
        every { sut.canLaunchChannel() } returns true // fixme: this mock doesnt work
        every {
            YouTubeIntents.createChannelIntent(mockActivity, fixtMedia.channelData.id!!.toString())
        } returns mockIntent

        every {
            YouTubeIntents.canResolveChannelIntent(any())
        } returns true

        every { Uri.parse(any()) } returns mockUri

        val actual = sut.launchChannel(fixtMedia)

        assertTrue(actual)
        verify {
            YouTubeIntents.createChannelIntent(
                mockActivity,
                fixtMedia.channelData.id!!.toString()
            )
        }
        verify { mockActivity.startActivity(mockIntent) }
    }

    @Test
    fun launchVideo() {
        every { sut.canLaunchVideo() } returns true
        every {
            YouTubeIntents.createPlayVideoIntent(mockActivity, fixtMedia.platformId)
        } returns mockIntent

        val actual = sut.launchVideo(fixtMedia)

        assertTrue(actual)
        verify { YouTubeIntents.createPlayVideoIntent(mockActivity, fixtMedia.platformId) }
        verify { mockActivity.startActivity(mockIntent) }
    }

    @Test
    fun testLaunchVideo() {
        every { sut.canLaunchVideoWithOptions() } returns true
        val fixtForceFullScreen = true
        val fixtFinishAfter = true
        every {
            YouTubeIntents.createPlayVideoIntentWithOptions(
                mockActivity,
                fixtMedia.platformId,
                fixtForceFullScreen,
                fixtFinishAfter
            )
        } returns mockIntent


        val actual = sut.launchVideo(fixtMedia, fixtForceFullScreen, fixtFinishAfter)

        assertTrue(actual)
        verify {
            YouTubeIntents.createPlayVideoIntentWithOptions(
                mockActivity,
                fixtMedia.platformId,
                fixtForceFullScreen,
                fixtFinishAfter
            )
        }
        verify { mockActivity.startActivity(mockIntent) }
    }

    @Test // todo
    fun testLaunchVideo_Fallback() {

    }


    @Test // todo
    fun testLaunchVideo_Fail() {

    }

    @Test // todo
    fun isApiAvailable() {
//        every{YouTubeApiServiceUtil.isYouTubeApiServiceAvailable(mockActivity)} returns SUCCESS
//
//        val actual = sut.isApiAvailable()
//
//        assertTrue(actual)
    }
}