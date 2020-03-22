package uk.co.sentinelweb.cuer.app.ui.playlist

import androidx.fragment.app.Fragment
import org.koin.android.scope.currentScope
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.R

class PlaylistFragment : Fragment(R.layout.fragment_playlist) {

    private val presenter: PlaylistPresenter by currentScope.inject()

    companion object {

        @JvmStatic
        val fragmentModule = module {
            scope(named<PlaylistFragment>()) {
                scoped<PlaylistContract.View> { getSource() }
                scoped<PlaylistContract.Presenter> { PlaylistPresenter(get(), get(), get(), get()) }
                scoped { PlaylistModelMapper() }
                scoped { PlaylistRepository() }
                viewModel { PlaylistState() }
            }
        }
    }
}
