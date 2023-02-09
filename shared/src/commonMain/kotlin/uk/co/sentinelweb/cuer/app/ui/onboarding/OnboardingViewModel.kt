package uk.co.sentinelweb.cuer.app.ui.onboarding

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.ui.onboarding.OnboardingContract.Label.Finished
import uk.co.sentinelweb.cuer.app.ui.onboarding.OnboardingContract.Label.Skip
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class OnboardingViewModel(
    private val state: OnboardingContract.State,
    private val config: OnboardingContract.Config,
    private val mapper: OnboardingMapper,
    private val log: LogWrapper,
    private val coroutines: CoroutineContextProvider
) : OnboardingContract.Interactions {
    init {
        log.tag(this)
    }

    private val _model: MutableStateFlow<OnboardingContract.Model>
    val model: StateFlow<OnboardingContract.Model> get() = _model

    private val _label: MutableSharedFlow<OnboardingContract.Label>
    val label: SharedFlow<OnboardingContract.Label> get() = _label

    init {
        state.config = config
        _model = MutableStateFlow(mapper.map(state))
        _label = MutableSharedFlow()
    }

    override fun onNext() {
        state.positionScreen = (state.positionScreen + 1)
        if (state.positionScreen < config.screens.size) {
            _model.value = mapper.map(state)
        } else {
            coroutines.mainScope.launch {
                _label.emit(Finished)
            }
        }
    }

    override fun onSkip() {
        coroutines.mainScope.launch {
            _label.emit(Skip)
        }
    }
}
