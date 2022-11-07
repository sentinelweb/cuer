package uk.co.sentinelweb.cuer.app.ui.onboarding

class OnboardingMapper {

    fun map(state: OnboardingContract.State) = with(state) {
        OnboardingContract.Model(
            screen = config.screens.get(positionScreen)
        )
    }
}
