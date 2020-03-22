package uk.co.sentinelweb.cuer.app.ui.player

import androidx.fragment.app.Fragment
import org.koin.android.scope.currentScope
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.R

class PlayerFragment : Fragment(R.layout.player_fragment) {

    private val presenter: PlayerPresenter by currentScope.inject()

    companion object {

        @JvmStatic
        val fragmentModule = module {
            scope(named<PlayerFragment>()) {
                scoped<PlayerContract.View> { getSource() }
                scoped<PlayerContract.Presenter> { PlayerPresenter(get(), get(), get(), get()) }
                scoped { PlayerModelMapper() }
                scoped { PlayerRepository() }
                viewModel { PlayerState() }
            }
        }
    }
}
