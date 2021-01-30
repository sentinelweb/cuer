package uk.co.sentinelweb.cuer.app.ui.share.scan

import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.FragmentScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.flextrade.jfixture.JFixture
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.util.wrapper.AndroidSnackbarWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.SnackbarWrapper
import uk.co.sentinelweb.cuer.tools.provider.FragmentScenarioProvider

@RunWith(AndroidJUnit4::class)
class ScanFragmentEspressoTest : FragmentScenarioProvider<ScanFragment> {

    @Mock
    lateinit var mockPresenter: ScanContract.Presenter

    private val fixture = JFixture()

    override fun get(): FragmentScenario<ScanFragment> =
        FragmentScenario.launchInContainer(ScanFragment::class.java, null, R.style.AppTheme, null)

    private lateinit var sharedTest: ScanFragmentSharedTest

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        sharedTest = ScanFragmentSharedTest(this, mockPresenter)
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
        sharedTest.fromShareUrl()
    }

    @Test
    fun showMessage() {
        val fixtMsg = fixture.create(String::class.java)
        get().onFragment { it.showMessage(fixtMsg) }
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(fixtMsg)))
    }

    @Test
    fun setModel_isLoading() {
        sharedTest.setModel_isLoading()
    }

    @Test
    fun setModel_isNotLoading() {
        sharedTest.setModel_isNotLoading()
    }

    @Test
    fun setResult() {
        sharedTest.setResult()
    }
}