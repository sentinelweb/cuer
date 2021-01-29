package uk.co.sentinelweb.cuer.app.ui.share.scan

import androidx.fragment.app.testing.FragmentScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.flextrade.jfixture.JFixture
import com.google.android.material.snackbar.Snackbar
import org.hamcrest.CoreMatchers
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.robolectric.annotation.Config
import uk.co.sentinelweb.cuer.app.CuerTestApp
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.util.wrapper.SnackbarWrapper
import uk.co.sentinelweb.cuer.tools.test.matchers.drawableMatches

@RunWith(AndroidJUnit4::class)
@Config(application = CuerTestApp::class)
class ScanFragmentTest {

    @Mock
    lateinit var mockPresenter: ScanContract.Presenter

    @Mock
    lateinit var mockSnackbarWrapper: SnackbarWrapper

    private val fixture = JFixture()

    private fun scenario(): FragmentScenario<ScanFragment> = FragmentScenario.launchInContainer(ScanFragment::class.java)

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        startKoin {
            modules(module {
                scope(named<ScanFragment>()) {
                    factory { mockPresenter }
                    factory { mockSnackbarWrapper }
                }
            })
        }
    }


    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun fromShareUrl() {
        val fixtUrl = fixture.create(String::class.java)

        scenario().onFragment { it.fromShareUrl(fixtUrl) }
        onView(withId(R.id.scan_progress)).check(matches(isDisplayed()))
        onView(withId(R.id.scan_result)).check(matches(CoreMatchers.not(isDisplayed())))
        verify(mockPresenter).fromShareUrl(fixtUrl)
    }

    @Test
    fun showMessage() {
        val fixtMsg = fixture.create(String::class.java)
        val scenario = FragmentScenario.launchInContainer(ScanFragment::class.java)
        val mockSnackbar = mock(Snackbar::class.java)
        `when`(mockSnackbarWrapper.make(fixtMsg)).thenReturn(mockSnackbar)
        scenario.onFragment { it.showMessage(fixtMsg) }
        verify(mockSnackbarWrapper).make(fixtMsg)
        verify(mockSnackbar).show()
    }

    @Test
    fun setModel_isLoading() {
        val fixtModel = fixture.create(ScanContract.Model::class.java).copy(
            isLoading = true
        )
        scenario().onFragment { it.setModel(fixtModel) }
        onView(withId(R.id.scan_progress)).check(matches(isDisplayed()))
        onView(withId(R.id.scan_result)).check(matches(CoreMatchers.not(isDisplayed())))
        onView(withId(R.id.scan_text)).check(matches(isDisplayed()))
        onView(withId(R.id.scan_text)).check(matches(ViewMatchers.withText(fixtModel.text)))
    }

    @Test
    fun setModel_isNotLoading() {
        val fixtModel = fixture.create(ScanContract.Model::class.java).copy(
            isLoading = false,
            resultIcon = R.drawable.ic_item_tick_white
        )
        scenario().onFragment { it.setModel(fixtModel) }
        onView(withId(R.id.scan_progress)).check(matches(CoreMatchers.not(isDisplayed())))
        onView(withId(R.id.scan_result)).check(matches(isDisplayed()))
        onView(withId(R.id.scan_text)).check(matches(isDisplayed()))
        onView(withId(R.id.scan_text)).check(matches(ViewMatchers.withText(fixtModel.text)))
        onView(withId(R.id.scan_result)).check(matches(drawableMatches(R.drawable.ic_item_tick_white)))
    }

    @Test
    fun setResult() {
        val fixtResult = fixture.create(ScanContract.Result::class.java)
        val mockListener = Mockito.mock(ScanContract.Listener::class.java)
        scenario().apply {
            onFragment { it.listener = mockListener }
            onFragment { it.setResult(fixtResult) }
        }
        verify(mockListener).scanResult(fixtResult)
    }
}