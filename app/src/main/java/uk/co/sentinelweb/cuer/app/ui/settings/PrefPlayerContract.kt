package uk.co.sentinelweb.cuer.app.ui.settings

import androidx.lifecycle.ViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

interface PrefPlayerContract {

    interface Presenter {
        var playerAutoFloat: Boolean
    }

    @Suppress("EmptyClassBlock")
    interface View {

    }

    data class State constructor(
        var x: Boolean = false
    ) : ViewModel()

    companion object {
        @JvmStatic
        val fragmentModule = module {
            scope(named<PrefPlayerFragment>()) {
                scoped<View> { get<PrefPlayerFragment>() }
                scoped<Presenter> {
                    PrefPlayerPresenter(
                        view = get(),
                        state = get(),
                        multiPrefs = get()
                    )
                }
                viewModel { State() }
            }
        }
    }
}
