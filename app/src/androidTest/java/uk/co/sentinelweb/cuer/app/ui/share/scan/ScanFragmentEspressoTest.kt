package uk.co.sentinelweb.cuer.app.ui.share.scan

import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.FragmentScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.flextrade.jfixture.JFixture
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.util.wrapper.AndroidSnackbarWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.SnackbarWrapper
import uk.co.sentinelweb.cuer.tools.test.matchers.drawableMatches

@RunWith(AndroidJUnit4::class)
class ScanFragmentEspressoTest {

    @Mock
    lateinit var mockPresenter: ScanContract.Presenter

    private val fixture = JFixture()

    private fun scenario(): FragmentScenario<ScanFragment> =
        FragmentScenario.launchInContainer(ScanFragment::class.java, null, R.style.AppTheme, null)

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        startKoin {
            modules(module {
                scope(named<ScanFragment>()) {
                    factory { mockPresenter }
                    factory<SnackbarWrapper> { AndroidSnackbarWrapper((getSource() as Fragment).requireActivity()) }
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
        onView(withId(R.id.scan_result)).check(matches(not(isDisplayed())))
        verify(mockPresenter).fromShareUrl(fixtUrl)
    }

    @Test
    fun showMessage() {
        val fixtMsg = fixture.create(String::class.java)
        scenario().onFragment { it.showMessage(fixtMsg) }
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(fixtMsg)))
    }

    @Test
    fun setModel_isLoading() {
        val fixtModel = fixture.create(ScanContract.Model::class.java).copy(
            isLoading = true
        )
        scenario().onFragment { it.setModel(fixtModel) }
        onView(withId(R.id.scan_progress)).check(matches(isDisplayed()))
        onView(withId(R.id.scan_result)).check(matches(not(isDisplayed())))
        onView(withId(R.id.scan_text)).check(matches(isDisplayed()))
        onView(withId(R.id.scan_text)).check(matches(withText(fixtModel.text)))
    }

    @Test
    fun setModel_isNotLoading() {
        val fixtModel = fixture.create(ScanContract.Model::class.java).copy(
            isLoading = false,
            resultIcon = R.drawable.ic_item_tick_white
        )
        scenario().onFragment { it.setModel(fixtModel) }
        onView(withId(R.id.scan_progress)).check(matches(not(isDisplayed())))
        onView(withId(R.id.scan_result)).check(matches(isDisplayed()))
        onView(withId(R.id.scan_text)).check(matches(isDisplayed()))
        onView(withId(R.id.scan_text)).check(matches(withText(fixtModel.text)))
        onView(withId(R.id.scan_result)).check(matches(drawableMatches(R.drawable.ic_item_tick_white)))
    }

    @Test
    fun setResult() {
        val fixtResult = fixture.create(ScanContract.Result::class.java)
        val mockListener = mock(ScanContract.Listener::class.java)
        scenario().apply {
            onFragment { it.listener = mockListener }
            onFragment { it.setResult(fixtResult) }
        }
        verify(mockListener).scanResult(fixtResult)
    }
}