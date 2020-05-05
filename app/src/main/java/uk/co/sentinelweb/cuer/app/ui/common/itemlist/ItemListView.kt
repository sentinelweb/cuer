package uk.co.sentinelweb.cuer.app.ui.common.itemlist

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import kotlinx.android.synthetic.main.view_item_list.view.*
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.KoinComponent
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module
import org.koin.ext.getOrCreateScope
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.common.itemlist.item.ItemContract
import uk.co.sentinelweb.cuer.app.ui.common.itemlist.item.ItemFactory

/**
 * This is a simple list linerlayout in scrollview (for small data sizes)
 */
class ItemListView constructor(
    c: Context,
    a: AttributeSet
) : FrameLayout(c, a),
    ItemListContract.View,
    KoinComponent {

    val presenter: ItemListContract.Presenter
    private val scope: Scope  by lazy { this.getOrCreateScope() }
    private val itemFactory: ItemFactory

    init {
        LayoutInflater.from(context).inflate(R.layout.view_item_list, this, true)
        presenter = scope.get()
        itemFactory = scope.get()
    }

    override fun addItem(interactions: ItemContract.Interactions): ItemContract.Presenter {
        return itemFactory.createPresenter(common_list, interactions)
    }

    override fun show(b: Boolean) {
        common_list_scroll.visibility = if (b) View.VISIBLE else View.GONE
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
                scoped<ItemListContract.Presenter> { ItemListPresenter(get(), get()) }
                viewModel { ItemListState() }
            }
        }
    }
}
