package uk.co.sentinelweb.cuer.app.ui.browse

import androidx.fragment.app.Fragment
import org.koin.android.scope.currentScope
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.R

class BrowseFragment : Fragment(R.layout.browse_fragment) {

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
