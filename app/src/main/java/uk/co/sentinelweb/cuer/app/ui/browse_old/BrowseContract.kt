package uk.co.sentinelweb.cuer.app.ui.browse_old

import androidx.lifecycle.ViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.domain.MediaDomain

interface BrowseContract {
    interface Presenter {

    }

    interface View {

    }


    data class Model constructor(
        val items: List<BrowseItemModel>
    ) {
        data class BrowseItemModel constructor(
            val url: String,
            val type: MediaDomain.MediaTypeDomain,
            val title: String,
            val length: String,
            val positon: String
        )
    }

    class State constructor(

    ) : ViewModel()

    companion object {

        @JvmStatic
        val fragmentModule = module {
            scope(named<BrowseFragment>()) {
                scoped<View> { getSource() }
                scoped<Presenter> { BrowsePresenter(get(), get(), get(), get()) }
                scoped { BrowseModelMapper() }
                scoped { BrowseRepository() }
                viewModel { State() }
            }
        }
    }
}