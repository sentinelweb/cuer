package uk.co.sentinelweb.cuer.app.ui.playlists.dialog

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.ui.common.item.ItemDiffCallback
import uk.co.sentinelweb.cuer.app.ui.playlists.item.ItemContract
import uk.co.sentinelweb.cuer.app.ui.playlists.item.ItemFactory
import uk.co.sentinelweb.cuer.app.ui.playlists.item.row.ItemRowViewHolder


class PlaylistsDialogAdapter constructor(
    private val itemFactory: ItemFactory,
    private val interactions: ItemContract.Interactions
) : RecyclerView.Adapter<ItemRowViewHolder>() {

    private lateinit var recyclerView: RecyclerView

    private var _data: List<ItemContract.Model> = listOf()

    val data: List<ItemContract.Model>
        get() = _data

    var currentPlaylistId: OrchestratorContract.Identifier<*>? = null

    fun setData(data: List<ItemContract.Model>, animate: Boolean = true) {
        if (animate) {
            DiffUtil.calculateDiff(
                ItemDiffCallback(
                    data,
                    this._data
                )
            ).apply {
                this@PlaylistsDialogAdapter._data = data
                // this stops random scrolling out of view
                val recyclerViewState = recyclerView.layoutManager?.onSaveInstanceState()
                dispatchUpdatesTo(this@PlaylistsDialogAdapter)
                recyclerView.layoutManager?.onRestoreInstanceState(recyclerViewState)
            }
        } else {
            this@PlaylistsDialogAdapter._data = data
            notifyDataSetChanged()
        }

    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = recyclerView
    }

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ItemRowViewHolder {
        return itemFactory.createItemViewHolder(parent, interactions)
    }

    @Override
    override fun onBindViewHolder(holderRow: ItemRowViewHolder, position: Int) {
        _data.get(position).apply {
            holderRow.itemPresenter.update(this as ItemContract.Model.ItemModel, this.id == currentPlaylistId?.id)
        }
    }

    override fun getItemCount(): Int = _data.size
}
