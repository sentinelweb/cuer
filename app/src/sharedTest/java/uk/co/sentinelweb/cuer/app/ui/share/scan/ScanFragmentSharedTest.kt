package uk.co.sentinelweb.cuer.app.ui.share.scan

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.appmattus.kotlinfixture.decorator.nullability.NeverNullStrategy
import com.appmattus.kotlinfixture.decorator.nullability.nullabilityStrategy
import com.appmattus.kotlinfixture.decorator.optional.NeverOptionalStrategy
import com.appmattus.kotlinfixture.decorator.optional.optionalStrategy
import com.appmattus.kotlinfixture.kotlinFixture
import org.hamcrest.CoreMatchers
import org.mockito.Mockito
import org.mockito.Mockito.verify
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.domain.*
import uk.co.sentinelweb.cuer.domain.creator.GuidCreator
import uk.co.sentinelweb.cuer.tools.provider.FragmentScenarioProvider
import uk.co.sentinelweb.cuer.tools.test.matchers.drawableMatches

class ScanFragmentSharedTest constructor(
    val scenario: FragmentScenarioProvider<ScanFragment>,
    val mockPresenter: ScanContract.Presenter
) {
    private val fixture = kotlinFixture {
        nullabilityStrategy(NeverNullStrategy)
        repeatCount { 5 }
        factory { OrchestratorContract.Identifier(GuidCreator().create(), fixture()) }
        optionalStrategy(NeverOptionalStrategy) {
            propertyOverride(PlaylistDomain::id, NeverOptionalStrategy)
            propertyOverride(PlaylistItemDomain::id, NeverOptionalStrategy)
            propertyOverride(MediaDomain::id, NeverOptionalStrategy)
            propertyOverride(ImageDomain::id, NeverOptionalStrategy)
            propertyOverride(ChannelDomain::id, NeverOptionalStrategy)
        }
        subType<Domain, MediaDomain>()
    }

    fun setup() {
    }

    fun fromShareUrl() {

        val fixtUrl: String = fixture()

        scenario.get().onFragment { it.fromShareUrl(fixtUrl) }
        onView(withId(R.id.scan_progress)).check(matches(isDisplayed()))
        onView(withId(R.id.scan_result)).check(matches(CoreMatchers.not(isDisplayed())))
        verify(mockPresenter).fromShareUrl(fixtUrl)
    }

    fun setModel_isLoading() {
        val fixtModel = fixture<ScanContract.Model>().copy(isLoading = true)
        scenario.get().onFragment { it.setModel(fixtModel) }
        onView(withId(R.id.scan_progress)).check(matches(isDisplayed()))
        onView(withId(R.id.scan_result)).check(matches(CoreMatchers.not(isDisplayed())))
        onView(withId(R.id.scan_text)).check(matches(isDisplayed()))
        onView(withId(R.id.scan_text)).check(matches(ViewMatchers.withText(fixtModel.text)))
    }

    fun setModel_isNotLoading() {
        val fixtModel = fixture<ScanContract.Model>().copy(
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
        val fixtResult = fixture<ScanContract.Result>()
        val mockListener = Mockito.mock(ScanContract.Listener::class.java)
        scenario.get().apply {
            onFragment { it.listener = mockListener }
            onFragment { it.setResult(fixtResult) }
        }
        verify(mockListener).scanResult(fixtResult)
    }
}