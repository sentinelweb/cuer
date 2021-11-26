package uk.co.sentinelweb.cuer.app.ui.share.scan

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.flextrade.jfixture.JFixture
import org.hamcrest.CoreMatchers
import org.mockito.Mockito
import org.mockito.Mockito.verify
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.domain.Domain
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.tools.provider.FragmentScenarioProvider
import uk.co.sentinelweb.cuer.tools.test.matchers.drawableMatches

class ScanFragmentSharedTest constructor(
    val scenario: FragmentScenarioProvider<ScanFragment>,
    val mockPresenter: ScanContract.Presenter
) {
    private val fixture = JFixture()

    fun setup() {
        fixture.customise()
            .lazyInstance(Domain::class.java, { fixture.create(MediaDomain::class.java) })
    }

    fun fromShareUrl() {

        val fixtUrl = fixture.create(String::class.java)

        scenario.get().onFragment { it.fromShareUrl(fixtUrl) }
        onView(withId(R.id.scan_progress)).check(matches(isDisplayed()))
        onView(withId(R.id.scan_result)).check(matches(CoreMatchers.not(isDisplayed())))
        verify(mockPresenter).fromShareUrl(fixtUrl)
    }

    fun setModel_isLoading() {
        val fixtModel = fixture.create(ScanContract.Model::class.java).copy(isLoading = true)
        scenario.get().onFragment { it.setModel(fixtModel) }
        onView(withId(R.id.scan_progress)).check(matches(isDisplayed()))
        onView(withId(R.id.scan_result)).check(matches(CoreMatchers.not(isDisplayed())))
        onView(withId(R.id.scan_text)).check(matches(isDisplayed()))
        onView(withId(R.id.scan_text)).check(matches(ViewMatchers.withText(fixtModel.text)))
    }

    fun setModel_isNotLoading() {
        val fixtModel = fixture.create(ScanContract.Model::class.java).copy(
            isLoading = false,
            resultIcon = R.drawable.ic_item_tick_white
        )
        scenario.get().onFragment { it.setModel(fixtModel) }
        onView(withId(R.id.scan_progress)).check(matches(CoreMatchers.not(isDisplayed())))
        onView(withId(R.id.scan_result)).check(matches(isDisplayed()))
        onView(withId(R.id.scan_text)).check(matches(isDisplayed()))
        onView(withId(R.id.scan_text)).check(matches(ViewMatchers.withText(fixtModel.text)))
        onView(withId(R.id.scan_result)).check(matches(drawableMatches(R.drawable.ic_item_tick_white)))
    }

    fun setResult() {
        val fixtResult = fixture.create(ScanContract.Result::class.java)
        val mockListener = Mockito.mock(ScanContract.Listener::class.java)
        scenario.get().apply {
            onFragment { it.listener = mockListener }
            onFragment { it.setResult(fixtResult) }
        }
        verify(mockListener).scanResult(fixtResult)
    }
}