package uk.co.sentinelweb.cuer.app.ui.playlist

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MotionEventCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.roche.mdas.util.wrapper.ToastWrapper
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.common.itemlist.helper.ItemTouchHelperAdapter
import uk.co.sentinelweb.cuer.app.ui.common.itemlist.helper.SimpleItemTouchHelperCallback
import uk.co.sentinelweb.cuer.app.ui.common.itemlist.item.ItemContract
import uk.co.sentinelweb.cuer.app.ui.common.itemlist.item.ItemDiffCallback
import uk.co.sentinelweb.cuer.app.ui.common.itemlist.item.ItemFactory
import uk.co.sentinelweb.cuer.app.ui.common.itemlist.item.ItemModel
import uk.co.sentinelweb.cuer.app.ui.playlist.PlaylistAdapter.ItemViewHolder
import java.util.*


class PlaylistAdapter constructor(
    private val itemFactory: ItemFactory,
    private val interactions: ItemContract.Interactions,
    private val toast: ToastWrapper,
    recyclerView: RecyclerView
) : RecyclerView.Adapter<ItemViewHolder>(), ItemTouchHelperAdapter {
    private val simpleItemTouchHelperCallback = SimpleItemTouchHelperCallback(this)

    // todo creator?
    private val itemTouchHelper = ItemTouchHelper(simpleItemTouchHelperCallback)

    init {
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

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
            .inflate(R.layout.view_playlist_item, parent, false)
        return ItemViewHolder(
            itemFactory.createPresenter(view as ItemContract.View, interactions),
            view
        )
    }

    @Override
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.itemPresenter.update(data.get(position))

        // Start a drag whenever the handle view it touched
        holder.itemView.findViewById<View>(R.id.listitem_overflow).setOnTouchListener { v, event ->
            // todo fix
            if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                itemTouchHelper.startDrag(holder)
            }
            false
        }
    }

    class ItemViewHolder(val itemPresenter: ItemContract.Presenter, view: View) :
        RecyclerView.ViewHolder(view)

    override fun getItemCount(): Int = data.size

    override fun onItemDismiss(position: Int) {
        toast.show("dismissed: $position")
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        Collections.swap(data, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
        return true
    }

}
