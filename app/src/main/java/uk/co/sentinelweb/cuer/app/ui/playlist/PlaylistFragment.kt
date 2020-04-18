package uk.co.sentinelweb.cuer.app.ui.playlist

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import org.koin.android.scope.currentScope
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.R

class PlaylistFragment : Fragment(R.layout.playlist_fragment),PlaylistContract.View {

    private val presenter: PlaylistContract.Presenter by currentScope.inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // presenter.initialise()
    }

    companion object {

        @JvmStatic
        val fragmentModule = module {
            scope(named<PlaylistFragment>()) {
                scoped<PlaylistContract.View> { getSource() }
                scoped<PlaylistContract.Presenter> { PlaylistPresenter(get(), get(), get(), get(), get()) }
                scoped { PlaylistModelMapper() }
                viewModel { PlaylistState() }
            }
        }
    }
}
