package uk.co.sentinelweb.cuer.app.ui.browse

import androidx.fragment.app.Fragment
import org.koin.android.scope.currentScope
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract
import uk.co.sentinelweb.cuer.app.ui.player.PlayerPresenter

class BrowseFragment : Fragment(R.layout.fragment_browse) {

    private val presenter: BrowsePresenter by currentScope.inject()

    companion object {

        @JvmStatic
        val fragmentModule = module {
            scope(named<BrowseFragment>()) {
                scoped<BrowseContract.View> { getSource() }
                scoped<BrowseContract.Presenter> { BrowsePresenter(get(), get(), get(), get()) }
                scoped { BrowseModelMapper() }
                scoped { BrowseRepository() }
                viewModel { BrowseState() }
            }
        }
    }
}
