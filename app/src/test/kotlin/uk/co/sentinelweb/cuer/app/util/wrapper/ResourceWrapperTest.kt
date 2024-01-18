package uk.co.sentinelweb.cuer.app.util.wrapper

import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.test.mock.MockContext
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ImageSpan
import android.util.DisplayMetrics
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.flextrade.jfixture.JFixture
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.annotation.Config
import uk.co.sentinelweb.cuer.app.CuerTestApp
import uk.co.sentinelweb.cuer.tools.ext.build
import uk.co.sentinelweb.cuer.tools.ext.buildCollection

@RunWith(AndroidJUnit4::class)
@Config(sdk = intArrayOf(34), application = CuerTestApp::class)
class ResourceWrapperTest {

    @Mock
    private lateinit var mockContext: MockContext

    @Mock
    lateinit var mockResources: Resources

    private val fixture = JFixture()
    private val displayMetrics: DisplayMetrics = fixture.build()
    private val fixtString: String = fixture.build()
    private val fixtId: Int = fixture.build()

    private lateinit var sut: ResourceWrapper

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        whenever(mockContext.resources).thenReturn(mockResources)
        whenever(mockResources.displayMetrics).thenReturn(displayMetrics)

        sut = ResourceWrapper(mockContext)

    }

    @Test
    fun getResources() {
        assertThat(sut.resources).isEqualTo(mockResources)
    }

    @Test
    fun getPixelDensity() {
        assertThat(sut.pixelDensity).isEqualTo(displayMetrics.density)
    }

    @Test
    fun getScreenWidth() {
        assertThat(sut.screenWidth).isEqualTo(displayMetrics.widthPixels)
    }

    @Test
    fun getScreenHeight() {
        assertThat(sut.screenHeight).isEqualTo(displayMetrics.heightPixels)
    }

    @Test
    fun getString() {
        whenever(mockContext.getString(fixtId)).thenReturn(fixtString)
        assertThat(sut.getString(fixtId)).isEqualTo(fixtString)
    }

    @Test
    fun getStringParams() {
        val params: List<Any> = fixture.buildCollection(3)
        whenever(mockResources.getString(fixtId, params)).thenReturn(fixtString)
        assertThat(sut.getString(fixtId, params)).isEqualTo(fixtString)
    }

    @Test
    fun getColor() {
        val color: Int = fixture.build()
        addColorResource(fixtId, color)
        assertThat(sut.getColor(fixtId)).isEqualTo(color)
    }

    @Test
    fun getDimensionPixelSize() {
        val pixelSize: Int = fixture.build()
        whenever(mockResources.getDimensionPixelSize(fixtId)).thenReturn(pixelSize)
        assertThat(sut.getDimensionPixelSize(fixtId)).isEqualTo(pixelSize)
    }

    @Test
    fun testGetDimensionPixelSize() {
        val dpSize: Float = fixture.build()
        assertThat(sut.getDimensionPixelSize(dpSize))
            .isEqualTo((displayMetrics.density * dpSize).toInt())
    }

    @Test
    fun getDrawable() {
        val mockDrawable: Drawable = mock()
        whenever(mockContext.getDrawable(fixtId)).thenReturn(mockDrawable)
        assertThat(sut.getDrawable(fixtId))
            .isEqualTo(mockDrawable)
    }

    @Test
    fun getDrawableTint() {
        val mockDrawable: Drawable = mock()
        val tintColor: Int = fixture.build()
        val tintColorId: Int = fixture.build()
        addColorResource(tintColorId, tintColor)
        whenever(mockContext.getDrawable(fixtId)).thenReturn(mockDrawable)

        assertThat(sut.getDrawable(fixtId, tintColorId))
            .isEqualTo(mockDrawable)
        verify(mockDrawable).setTint(tintColor)
    }

    @Test
    fun getDrawableTextBounds() {
        val mockDrawable: Drawable = mock()
        val tintColor: Int = fixture.build()
        val tintColorId: Int = fixture.build()
        addColorResource(tintColorId, tintColor)
        val dimen: Int = fixture.build()
        val dimenId: Int = fixture.build()
        whenever(mockContext.getDrawable(fixtId)).thenReturn(mockDrawable)
        whenever(mockResources.getDimensionPixelSize(dimenId)).thenReturn(dimen)
        val scale: Float = fixture.build()

        assertThat(sut.getDrawable(fixtId, tintColorId, dimenId, scale))
            .isEqualTo(mockDrawable)
        verify(mockDrawable).setTint(tintColor)
        val scaledSize = (dimen * scale).toInt()
        verify(mockDrawable).setBounds(0, 0, scaledSize, scaledSize)

    }

    @Test
    fun replaceSpannableIcon() {
        val mockDrawable: Drawable = mock()
        val mockSpannableString: SpannableString = mock()
        val start: Int = fixture.build()
        val end: Int = fixture.build()
        sut.replaceSpannableIcon(mockSpannableString, mockDrawable, start, end, ImageSpan.ALIGN_BOTTOM)
        val imgSpanCapture = argumentCaptor<ImageSpan>()
        verify(mockSpannableString).setSpan(imgSpanCapture.capture(), eq(start), eq(end), eq(Spannable.SPAN_INCLUSIVE_INCLUSIVE))
        assertThat(imgSpanCapture.firstValue.drawable).isEqualTo(mockDrawable)
    }

    @Test
    fun getIntArray() {
        val ints: List<Int> = fixture.buildCollection()
        whenever(mockResources.getIntArray(fixtId)).thenReturn(ints.toIntArray())
        assertThat(sut.getIntArray(fixtId)).isEqualTo(ints)
    }

    private fun addColorResource(id: Int, color: Int) {
        whenever(mockContext.getColor(id)).thenReturn(color)
    }
}