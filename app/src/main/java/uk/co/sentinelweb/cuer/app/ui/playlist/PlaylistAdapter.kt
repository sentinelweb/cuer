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

    var data: List<ItemModel> = listOf()
        get() = field
        set(value) {
            DiffUtil.calculateDiff(ItemDiffCallback(value, field)).apply {
                field = value
                // this stops random scrolling out of view
                val recyclerViewState = recyclerView.layoutManager?.onSaveInstanceState()
                dispatchUpdatesTo(this@PlaylistAdapter)
                recyclerView.layoutManager?.onRestoreInstanceState(recyclerViewState)
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
        holder.itemPresenter.update(data.get(position))
    }

    override fun getItemCount(): Int = data.size
}
