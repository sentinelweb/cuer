package uk.co.sentinelweb.cuer.app.ui.onboarding

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class OnboardingViewModel(
    private val state: OnboardingContract.State,
    private val config: OnboardingContract.Config,
    private val mapper: OnboardingMapper,
) {
    val _model: MutableStateFlow<OnboardingContract.Model>
    val model: Flow<OnboardingContract.Model> get() = _model

    init {
        state.config = config
        state.positionScreen = 0
        _model = MutableStateFlow(mapper.map(state))
    }

    fun onNext() {
        state.positionScreen++
        _model.value = mapper.map(state)
    }
}
