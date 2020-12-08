package uk.co.sentinelweb.cuer.app.ui.playlist

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import uk.co.sentinelweb.cuer.app.ui.playlist.item.*


class PlaylistAdapter constructor(
    private val itemFactory: ItemFactory,
    private val interactions: ItemContract.Interactions
) : RecyclerView.Adapter<ItemViewHolder>() {

    private lateinit var recyclerView: RecyclerView

    private var _data: List<ItemModel> = listOf()

    val data: List<ItemModel>
        get() = _data

    fun setData(data: List<ItemModel>, animate: Boolean = true) {
        if (animate) {
            DiffUtil.calculateDiff(ItemDiffCallback(data, this._data)).apply {
                this@PlaylistAdapter._data = data
                // this stops random scrolling out of view
                val recyclerViewState = recyclerView.layoutManager?.onSaveInstanceState()
                dispatchUpdatesTo(this@PlaylistAdapter)
                recyclerView.layoutManager?.onRestoreInstanceState(recyclerViewState)
            }
        } else {
            this@PlaylistAdapter._data = data
            notifyDataSetChanged()
        }

    }
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = recyclerView
    }
    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ItemViewHolder {
        return itemFactory.createItemViewHolder(parent, interactions)
    }

    @Override
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.itemPresenter.update(_data.get(position))
    }

    override fun getItemCount(): Int = _data.size
}
