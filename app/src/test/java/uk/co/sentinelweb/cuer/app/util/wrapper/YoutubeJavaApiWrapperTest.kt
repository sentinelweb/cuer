package uk.co.sentinelweb.cuer.app.util.wrapper

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.flextrade.jfixture.JFixture
import com.google.android.youtube.player.YouTubeIntents
import io.mockk.*
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import uk.co.sentinelweb.cuer.domain.MediaDomain

// todo make another test with robolectric activity spy
class YoutubeJavaApiWrapperTest {

    private var mockActivity: AppCompatActivity = mockk(relaxUnitFun = true)
    private var mockIntent: Intent = mockk(relaxUnitFun = true)
    private val fixture: JFixture = JFixture()
    private val fixtMedia: MediaDomain = fixture.create(MediaDomain::class.java)

    private var sut: YoutubeJavaApiWrapper = spyk(YoutubeJavaApiWrapper(mockActivity))

    @Before
    fun setUp() {
        mockkStatic("com.google.android.youtube.player.YouTubeIntents")
        //mockkStatic("com.google.android.youtube.player.YouTubeApiServiceUtil")
    }

    @Test
    fun launchChannel() {
        every { sut.canLaunchChannel() } returns true
        every {
            YouTubeIntents.createChannelIntent(mockActivity, fixtMedia.channelData.id)
        } returns mockIntent

        val actual = sut.launchChannel(fixtMedia)

        assertTrue(actual)
        verify { YouTubeIntents.createChannelIntent(mockActivity, fixtMedia.channelData.id) }
        verify { mockActivity.startActivity(mockIntent) }
    }

    @Test
    fun launchVideo() {
        every { sut.canLaunchVideo() } returns true
        every {
            YouTubeIntents.createPlayVideoIntent(mockActivity, fixtMedia.remoteId)
        } returns mockIntent

        val actual = sut.launchVideo(fixtMedia)

        assertTrue(actual)
        verify { YouTubeIntents.createPlayVideoIntent(mockActivity, fixtMedia.remoteId) }
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
                fixtMedia.remoteId,
                fixtForceFullScreen,
                fixtFinishAfter
            )
        } returns mockIntent


        val actual = sut.launchVideo(fixtMedia, fixtForceFullScreen, fixtFinishAfter)

        assertTrue(actual)
        verify {
            YouTubeIntents.createPlayVideoIntentWithOptions(
                mockActivity,
                fixtMedia.remoteId,
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