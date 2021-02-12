package uk.co.sentinelweb.cuer.app.util.share.scan

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import uk.co.sentinelweb.cuer.app.CuerTestApp
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.core.wrapper.SystemLogWrapper
import uk.co.sentinelweb.cuer.domain.ObjectTypeDomain.MEDIA

@RunWith(AndroidJUnit4::class)
@Config(sdk = intArrayOf(28), application = CuerTestApp::class)
class LinkScannerTest {
    @MockK
    lateinit var mockUrlMediaMapper: UrlMediaMapper

    private val log: LogWrapper = SystemLogWrapper()
    private lateinit var sut: LinkScanner

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        sut = LinkScanner(log, listOf(mockUrlMediaMapper))
    }

    @After
    fun tearDown() {
    }

    @Test
    fun `scan - basic`() {
        val expectedLink = "https://youtu.be/gim0Q5-zCRk"
        every { mockUrlMediaMapper.check(Uri.parse(expectedLink)) } returns true
        every { mockUrlMediaMapper.map(Uri.parse(expectedLink)) } returns (MEDIA to expectedLink)

        val actual = sut.scan(expectedLink)

        assertThat(actual).isNotNull()
        assertThat(actual?.first).isEqualTo(MEDIA)
        assertThat(actual?.second).isEqualTo(expectedLink)
    }

    @Test
    fun `scan - clean share with text`() {
        val fixtText = """Hilt - Dependency Injection on Android #1: Introduction to Hilt
https://youtu.be/gim0Q5-zCRk


by "hitherejoe" (https://www.youtube.com/channel/585
) """
        val expectedLink = "https://youtu.be/gim0Q5-zCRk"
        every { mockUrlMediaMapper.check(Uri.parse(expectedLink)) } returns true
        every { mockUrlMediaMapper.map(Uri.parse(expectedLink)) } returns (MEDIA to expectedLink)

        val actual = sut.scan(fixtText)

        assertThat(actual).isNotNull()
        assertThat(actual?.first).isEqualTo(MEDIA)
        assertThat(actual?.second).isEqualTo(expectedLink)
    }
}