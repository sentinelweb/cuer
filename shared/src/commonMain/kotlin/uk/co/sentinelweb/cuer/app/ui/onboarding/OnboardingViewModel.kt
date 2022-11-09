package uk.co.sentinelweb.cuer.app.ui.onboarding

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.ui.onboarding.OnboardingContract.Event.Finished
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class OnboardingViewModel(
    private val state: OnboardingContract.State,
    private val config: OnboardingContract.Config,
    private val mapper: OnboardingMapper,
    private val log: LogWrapper,
    private val coroutineScope: CoroutineContextProvider
) : OnboardingContract.Interactions {
    init {
        log.tag(this)
    }

    private val _model: MutableStateFlow<OnboardingContract.Model>
    val model: StateFlow<OnboardingContract.Model> get() = _model

    private val _event: MutableSharedFlow<OnboardingContract.Event>
    val event: SharedFlow<OnboardingContract.Event> get() = _event

    init {
        state.config = config
        _model = MutableStateFlow(mapper.map(state))
        _event = MutableSharedFlow()
    }

    override fun onNext() {
        state.positionScreen = (state.positionScreen + 1)
        if (state.positionScreen < config.screens.size) {
            _model.value = mapper.map(state)
        } else {
            coroutineScope.mainScope.launch {
                _event.emit(Finished)
            }
        }
    }

    override fun onSkip() {
        coroutineScope.mainScope.launch {
            _event.emit(Finished)
        }
    }
}
