package uk.co.sentinelweb.cuer.app.ui.onboarding

import uk.co.sentinelweb.cuer.app.ui.resources.ActionResources

class OnboardingContract {

    data class Config(
        val screens: List<Screen>
    ) {
        data class Screen(
            val title: ActionResources,
            val lines: List<ActionResources>,
        )
    }

    data class State(
        var positionScreen: Int = 0,
        var config: Config
    )

    data class Model(
        val screen: Config.Screen,
        val screenPosition: String
    )

    enum class Event { Finished }
}
