package uk.co.sentinelweb.cuer.app.ui.share.scan

import androidx.fragment.app.testing.FragmentScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.flextrade.jfixture.JFixture
import com.google.android.material.snackbar.Snackbar
import com.nhaarman.mockitokotlin2.whenever
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
import org.robolectric.annotation.Config
import uk.co.sentinelweb.cuer.app.CuerTestApp
import uk.co.sentinelweb.cuer.app.util.wrapper.SnackbarWrapper
import uk.co.sentinelweb.cuer.tools.provider.FragmentScenarioProvider

@RunWith(AndroidJUnit4::class)
@Config(application = CuerTestApp::class)
class ScanFragmentTest : FragmentScenarioProvider<ScanFragment> {

    @Mock
    lateinit var mockPresenter: ScanContract.Presenter

    @Mock
    lateinit var mockSnackbarWrapper: SnackbarWrapper

    private val fixture = JFixture()

    override fun get(): FragmentScenario<ScanFragment> =
        FragmentScenario.launchInContainer(ScanFragment::class.java)

    private lateinit var sharedTest: ScanFragmentSharedTest

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        sharedTest = ScanFragmentSharedTest(this, mockPresenter)
        startKoin {
            modules(module {
                scope(named<ScanFragment>()) {
                    factory { mockPresenter }
                    factory { mockSnackbarWrapper }
                }
            })
        }
        sharedTest.setup()
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
        val scenario = FragmentScenario.launchInContainer(ScanFragment::class.java)
        val mockSnackbar = mock(Snackbar::class.java)
        whenever(mockSnackbarWrapper.make(fixtMsg)).thenReturn(mockSnackbar)
        scenario.onFragment { it.showMessage(fixtMsg) }
        verify(mockSnackbarWrapper).make(fixtMsg)
        verify(mockSnackbar).show()
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