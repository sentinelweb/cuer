package uk.co.sentinelweb.cuer.app.ui.player_fragment

import androidx.lifecycle.ViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.domain.MediaDomain

interface PlayerFragmentContract {
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

    class ModelMapper constructor() {
        fun map(domain: MediaDomain): PlayerFragmentContract.Model =
            PlayerFragmentContract.Model(
                domain.url.toString(),
                domain.mediaType,
                domain.title ?: "-",
                domain.duration?.let { "${(it / 1000)}s" } ?: "-",
                domain.positon?.let { "${(it / 1000)}s" } ?: "-"
            )
    }

    companion object {

        @JvmStatic
        val fragmentModule = module {
            scope(named<PlayerFragment>()) {
                scoped<View> { getSource() }
                scoped<Presenter> { PlayerFragmentPresenter(get(), get(), get()) }
                scoped { ModelMapper() }
                viewModel { State() }
            }
        }
    }
}