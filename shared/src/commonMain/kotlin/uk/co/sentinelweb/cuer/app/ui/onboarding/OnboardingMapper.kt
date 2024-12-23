package uk.co.sentinelweb.cuer.app.ui.onboarding

class OnboardingMapper {

    fun map(state: OnboardingContract.State) = with(state) {
        OnboardingContract.Model(
            screen = config.screens.get(positionScreen),
            screenPosition = "${positionScreen + 1} / ${config.screens.size}",
            isLastScreen = positionScreen == config.screens.size - 1
        )
    }
}
