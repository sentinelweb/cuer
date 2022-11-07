package uk.co.sentinelweb.cuer.app.ui.onboarding

class OnboardingContract {

    data class Config(
        val screens: List<Screen>
    ) {
        data class Screen(
            val title: String,
            val lines: List<Line>,
        ) {
            data class Line(
                val text: String,
                val icon: Int,
            )
        }
    }

    data class State(
        var positionScreen: Int,
        var config: Config
    )

    data class Model(
        val screen: Config.Screen
    )
}
