package uk.co.sentinelweb.cuer.app.ui.playlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.playlist.PlaylistAdapter.ItemViewHolder
import uk.co.sentinelweb.klink.ui.common.itemlist.item.ItemContract
import uk.co.sentinelweb.klink.ui.common.itemlist.item.ItemFactory
import uk.co.sentinelweb.klink.ui.common.itemlist.item.ItemModel

class PlaylistAdapter constructor(
    private val itemFactory: ItemFactory,
    private val interactions: ItemContract.Interactions
) :
    RecyclerView.Adapter<ItemViewHolder>() {
    var data: List<ItemModel>? = null
        get() = field
        set(value) {
            field = value
            notifyDataSetChanged()
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
        holder.itemPresenter.update(data?.get(position) ?: UNKNOWN_ITEM)
    }

    class ItemViewHolder(val itemPresenter: ItemContract.Presenter, view: View) :
        RecyclerView.ViewHolder(view)

    override fun getItemCount(): Int =
        data?.size ?: 0

    companion object {
        private val UNKNOWN_ITEM = ItemModel("0", "top", "bottom", false, R.drawable.ic_play_black)
    }

}
