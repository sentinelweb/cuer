package uk.co.sentinelweb.cuer.app.ui.share.scan

import androidx.fragment.app.testing.FragmentScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.flextrade.jfixture.JFixture
import io.mockk.mockk
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.mockito.MockitoAnnotations
import org.robolectric.annotation.Config
import uk.co.sentinelweb.cuer.app.CuerTestApplication
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.util.wrapper.SnackbarWrapper

@RunWith(AndroidJUnit4::class)
@Config(application = CuerTestApplication::class)
class ScanFragmentTest {

    private var mockPresenter: ScanContract.Presenter = mockk(relaxUnitFun = true)
    private var mockSnackbarWrapper: SnackbarWrapper = mockk(relaxUnitFun = true)

    private val fixture = JFixture()

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
        val mockUrl = fixture.create(String::class.java)
        val scenario = FragmentScenario.launchInContainer(ScanFragment::class.java)
        //scenario.moveToState(Lifecycle.State.RESUMED)
        scenario.onFragment { it.fromShareUrl(mockUrl) }
        onView(withId(R.id.scan_progress)).check(matches(isDisplayed()))
        verify {
            mockPresenter.fromShareUrl(mockUrl)
        }
    }

    @Test
    fun showMessage() {
    }

    @Test
    fun setModel() {
    }

    @Test
    fun setResult() {
    }
}