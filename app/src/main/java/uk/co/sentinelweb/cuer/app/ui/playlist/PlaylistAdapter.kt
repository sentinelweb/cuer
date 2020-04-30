package uk.co.sentinelweb.cuer.app.ui.playlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.common.itemlist.item.ItemContract
import uk.co.sentinelweb.cuer.app.ui.common.itemlist.item.ItemDiffCallback
import uk.co.sentinelweb.cuer.app.ui.common.itemlist.item.ItemFactory
import uk.co.sentinelweb.cuer.app.ui.common.itemlist.item.ItemModel
import uk.co.sentinelweb.cuer.app.ui.playlist.PlaylistAdapter.ItemViewHolder


class PlaylistAdapter constructor(
    private val itemFactory: ItemFactory,
    private val interactions: ItemContract.Interactions
) : RecyclerView.Adapter<ItemViewHolder>() {

    var data: List<ItemModel> = listOf()
        get() = field
        set(value) {
            DiffUtil.calculateDiff(ItemDiffCallback(value, field)).apply {
                field = value
                dispatchUpdatesTo(this@PlaylistAdapter)
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_media_list_item, parent, false)
        return ItemViewHolder(
            itemFactory.createPresenter(view as ItemContract.View, interactions),
            view
        )
    }

    @Override
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.itemPresenter.update(data.get(position))
    }

    class ItemViewHolder(val itemPresenter: ItemContract.Presenter, view: View) :
        RecyclerView.ViewHolder(view)

    override fun getItemCount(): Int = data.size

    companion object {
        private val UNKNOWN_ITEM =
            ItemModel("0", "top", "bottom", false, R.drawable.ic_nav_play_black, null)
    }

}
