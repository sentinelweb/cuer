package uk.co.sentinelweb.cuer.app.ui.onboarding

import kotlinx.serialization.Serializable
import uk.co.sentinelweb.cuer.app.ui.common.resources.ActionResources

class OnboardingContract {

    @Serializable
    data class Config(
        val screens: List<Screen>
    ) {
        @Serializable
        data class Screen(
            val title: ActionResources,
            val subtitle: String? = null,
            val lines: List<ActionResources>,
            val backgroundUrl: String? = null,
            val backgroundColor: Int? = null
        )
    }

    data class State(
        var positionScreen: Int = 0,
        var config: Config
    )

    data class Model(
        val screen: Config.Screen,
        val screenPosition: String,
        val isLastScreen: Boolean
    )

    interface Interactions {
        fun onNext()

        fun onSkip()
    }

    interface ConfigBuilder {
        fun build(): Config
    }

    enum class Label { Skip, Finished }
}
