package uk.co.sentinelweb.cuer.app.ui.player

import androidx.fragment.app.Fragment
import org.koin.android.scope.currentScope
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract
import uk.co.sentinelweb.cuer.app.ui.player.PlayerPresenter

class PlayerFragment : Fragment(R.layout.fragment_player) {

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
