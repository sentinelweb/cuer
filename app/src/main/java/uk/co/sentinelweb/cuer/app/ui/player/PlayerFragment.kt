package uk.co.sentinelweb.cuer.app.ui.player

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.roche.mdas.util.wrapper.ToastWrapper
import kotlinx.android.synthetic.main.player_fragment.*
import org.koin.android.ext.android.inject
import org.koin.android.scope.currentScope
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.common.itemlist.item.ItemContract
import uk.co.sentinelweb.cuer.app.ui.common.itemlist.item.ItemModel

class PlayerFragment : Fragment(R.layout.player_fragment), PlayerContract.View {

    private val presenter: PlayerContract.Presenter by currentScope.inject()
    private val toastWrapper: ToastWrapper by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // this code is just to test the item list - can be removed
        player_list_test.presenter.setInteractions(object: ItemContract.Interactions{
            override fun onClick(item: ItemModel) {
                toastWrapper.showToast("click: ${item.topText}")
            }

            override fun onRightSwipe(item: ItemModel) {
                toastWrapper.showToast("right: ${item.topText}")
            }

            override fun onLeftSwipe(item: ItemModel) {
                toastWrapper.showToast("left: ${item.topText}")
            }
        })
        player_list_test.presenter.bind(
            listOf(
                ItemModel("","top","bottom",false,R.drawable.ic_play_black),
                ItemModel("","top2","bottom2",false,R.drawable.ic_browse_black),
                ItemModel("","top3","bottom3",false,R.drawable.ic_player_fast_forward_black),
                ItemModel("","top4","bottom4",false,R.drawable.ic_player_fast_rewind_black)
            )
        )
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
