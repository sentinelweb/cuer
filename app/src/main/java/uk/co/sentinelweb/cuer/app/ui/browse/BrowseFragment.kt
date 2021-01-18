package uk.co.sentinelweb.cuer.app.ui.browse

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.browse_fragment.*
import org.koin.android.scope.currentScope
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.R
// fixme : this is the app:startDestination fragment which causes a double instance and the menus not to work :/
// when implemnenting here might need to make a dummy app:startDestination and  manually navigate in mainActivity
class BrowseFragment : Fragment(R.layout.browse_fragment), BrowseContract.View {

    private val presenter: BrowseContract.Presenter by currentScope.inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        browse_toolbar.let {
            (activity as AppCompatActivity).setSupportActionBar(it)
            //it.setupWithNavController(findNavController(), AppBarConfiguration(TOP_LEVEL_DESTINATIONS))
        }
    }

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
