package uk.co.sentinelweb.cuer.app.ui.player

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.roche.mdas.util.wrapper.ToastWrapper
import org.koin.android.ext.android.inject
import org.koin.android.scope.currentScope
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.R

class PlayerFragment : Fragment(R.layout.player_fragment), PlayerContract.View {

    private val presenter: PlayerContract.Presenter by currentScope.inject()
    private val toastWrapper: ToastWrapper by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
    companion object {

        @JvmStatic
        val fragmentModule = module {
            scope(named<PlayerFragment>()) {
                scoped<PlayerContract.View> { getSource() }
                scoped<PlayerContract.Presenter> { PlayerPresenter(get(), get(), get()) }
                scoped { PlayerModelMapper() }
                viewModel { PlayerState() }
            }
        }
    }
}
