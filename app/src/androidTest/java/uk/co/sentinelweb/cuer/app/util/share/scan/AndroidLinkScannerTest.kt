package uk.co.sentinelweb.cuer.app.util.share.scan

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import uk.co.sentinelweb.cuer.app.util.wrapper.log.AndroidLogWrapper
import uk.co.sentinelweb.cuer.domain.ChannelDomain
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.NO_PLATFORM_ID
import uk.co.sentinelweb.cuer.domain.ObjectTypeDomain

@RunWith(AndroidJUnit4::class)
class AndroidLinkScannerTest {
    val sut = AndroidLinkScanner(log = AndroidLogWrapper(), mappers = urlMediaMappers)

    @Test
    fun shorts_url() {
        val expectedLink = "https://www.youtube.com/shorts/lq9hzALa4Po?feature=share"

        val actual = sut.scan(expectedLink)
        val expectedMedia = MediaDomain.createYoutube(expectedLink, "lq9hzALa4Po")
        assertThat(actual).isNotNull()
        assertThat(actual?.first).isEqualTo(ObjectTypeDomain.MEDIA)
        assertThat(actual?.second).isEqualTo(expectedMedia)
    }

    @Test
    fun shorts_url_with_c() {
        val expectedLink = "https://youtube.com/shorts/WsciBZQACk4?feature=share"

        val actual = sut.scan(expectedLink)
        val expectedMedia = MediaDomain.createYoutube(expectedLink, "WsciBZQACk4")
        assertThat(actual).isNotNull()
        assertThat(actual?.first).isEqualTo(ObjectTypeDomain.MEDIA)
        assertThat(actual?.second).isEqualTo(expectedMedia)
    }

    @Test
    fun channel_url_with_c() {
        val expectedLink = "https://www.youtube.com/c/MattGreenComedy"

        val actual = sut.scan(expectedLink)
        val expectedChannel = ChannelDomain.createYoutubeCustomUrl("/c/MattGreenComedy")
        assertThat(actual).isNotNull()
        assertThat(actual?.first).isEqualTo(ObjectTypeDomain.CHANNEL)
        assertThat(actual?.second).isEqualTo(expectedChannel)
        val actualChannel = actual!!.second as ChannelDomain
        assertThat(actualChannel.platformId).isEqualTo(NO_PLATFORM_ID)
        assertThat(actualChannel.customUrl).isEqualTo("MattGreenComedy")
    }

    @Test
    fun channel_url_with_id() {
        val expectedLink = "https://www.youtube.com/channel/UCM191aISRy5AQ51wCXOGiEg"

        val actual = sut.scan(expectedLink)
        val expectedChannel = ChannelDomain.createYoutube("/channel/UCM191aISRy5AQ51wCXOGiEg")
        assertThat(actual).isNotNull()
        assertThat(actual?.first).isEqualTo(ObjectTypeDomain.CHANNEL)
        assertThat(actual?.second).isEqualTo(expectedChannel)
        val actualChannel = actual!!.second as ChannelDomain
        assertThat(actualChannel.platformId).isEqualTo("UCM191aISRy5AQ51wCXOGiEg")
        assertThat(actualChannel.customUrl).isNull()
    }

    @Test
    fun media_watch_url() {
        val expectedLink = "https://www.youtube.com/watch?v=JqsQ_JjiFmg"

        val actual = sut.scan(expectedLink)
        val expectedMedia = MediaDomain.createYoutube(expectedLink, "JqsQ_JjiFmg")
        assertThat(actual).isNotNull()
        assertThat(actual?.first).isEqualTo(ObjectTypeDomain.MEDIA)
        assertThat(actual?.second).isEqualTo(expectedMedia)
        val actualMedia = actual!!.second as MediaDomain
        assertThat(actualMedia.platformId).isEqualTo("JqsQ_JjiFmg")
    }

    @Test
    fun media_short_url() {
        val expectedLink = "https://youtu.be/JqsQ_JjiFmg"

        val actual = sut.scan(expectedLink)
        val expectedMedia = MediaDomain.createYoutube(expectedLink, "JqsQ_JjiFmg")
        assertThat(actual).isNotNull()
        assertThat(actual?.first).isEqualTo(ObjectTypeDomain.MEDIA)
        assertThat(actual?.second).isEqualTo(expectedMedia)
        val actualMedia = actual!!.second as MediaDomain
        assertThat(actualMedia.platformId).isEqualTo("JqsQ_JjiFmg")
    }
}
