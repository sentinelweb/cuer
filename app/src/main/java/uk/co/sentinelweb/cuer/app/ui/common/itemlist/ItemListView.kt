package uk.co.sentinelweb.klink.ui.common.itemlist

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import kotlinx.android.synthetic.main.view_item_list.view.*
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.KoinComponent
import org.koin.android.scope.currentScope
import org.koin.core.get
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.ext.getOrCreateScope
import org.koin.ext.scope
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.player.PlayerModelMapper
import uk.co.sentinelweb.klink.ui.common.itemlist.item.ItemContract
import uk.co.sentinelweb.klink.ui.common.itemlist.item.ItemFactory

/**
 * This is a false list linerlayout in scrollview (for small data sizes)
 */
class ItemListView constructor(
    c: Context,
    a: AttributeSet
) : FrameLayout(c, a),
    ItemListContract.View,
    KoinComponent {

    val presenter: ItemListContract.Presenter
    val itemFactory: ItemFactory

    init {
        LayoutInflater.from(context).inflate(R.layout.view_item_list, this, true)
        // TODO check this works !!!
        val scope = this.getOrCreateScope()
// Get scoped instances from `a`
        presenter = scope.get()
        itemFactory = scope.get()
//        presenter = injector.createPresenter(this )
//        itemFactory = injector.itemFactory()
    }

    override fun addItem(interactions: ItemContract.Interactions): ItemContract.Presenter {
        return itemFactory.createPresenter(common_list, interactions)
    }

    override fun show(b: Boolean) {
        url_list_scroll.visibility = if (b) View.VISIBLE else View.GONE
    }

    override fun clearFrom(index: Int) {
        while (common_list.childCount > index) {
            common_list.removeViewAt(index)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        scope.close()
    }

    companion object {

        @JvmStatic
        val viewModule = module {
            scope(named<ItemListView>()) {
                scoped<ItemListContract.View> { getSource() }
                scoped<ItemListContract.Presenter> { ItemListPresenter(get()) }
                scoped { PlayerModelMapper() }
                scoped { PlayerModelMapper() }
                viewModel { ItemListState() }
            }
        }
    }
}
