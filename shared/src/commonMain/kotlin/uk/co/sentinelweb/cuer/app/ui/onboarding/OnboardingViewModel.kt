package uk.co.sentinelweb.cuer.app.ui.onboarding

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import uk.co.sentinelweb.cuer.app.ui.onboarding.OnboardingContract.Event.Finished

class OnboardingViewModel(
    private val state: OnboardingContract.State,
    private val config: OnboardingContract.Config,
    private val mapper: OnboardingMapper,
) {

    val _model: MutableStateFlow<OnboardingContract.Model>
    val model: StateFlow<OnboardingContract.Model> get() = _model

    val _event: MutableSharedFlow<OnboardingContract.Event>
    val event: SharedFlow<OnboardingContract.Event> get() = _event

    init {
        state.config = config
        _model = MutableStateFlow(mapper.map(state))
        _event = MutableSharedFlow()
    }

    fun onNext() {
        state.positionScreen = (state.positionScreen + 1) % config.screens.size
        if (state.positionScreen < config.screens.size) {
            _model.value = mapper.map(state)
        } else {
            _event.tryEmit(Finished)
        }
    }
}
