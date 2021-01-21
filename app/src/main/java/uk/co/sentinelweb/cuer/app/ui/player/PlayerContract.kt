package uk.co.sentinelweb.cuer.app.ui.player

import androidx.lifecycle.ViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.domain.MediaDomain

interface PlayerContract {
    interface Presenter {

    }

    interface View {

    }

    class State constructor(

    ) : ViewModel()


    data class Model constructor(
        val url: String,
        val type: MediaDomain.MediaTypeDomain,
        val title: String,
        val length: String,
        val positon: String
    )

    companion object {

        @JvmStatic
        val fragmentModule = module {
            scope(named<PlayerFragment>()) {
                scoped<View> { getSource() }
                scoped<Presenter> { PlayerPresenter(get(), get(), get()) }
                scoped { PlayerModelMapper() }
                viewModel { State() }
            }
        }
    }
}