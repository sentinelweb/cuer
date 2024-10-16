/*
 * Copyright (C) 2015 Paul Burke
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.co.sentinelweb.cuer.app.ui.common.item

import android.graphics.Canvas
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import uk.co.sentinelweb.cuer.app.ui.common.item.ItemBaseContract.ItemTouchHelperViewHolder
import uk.co.sentinelweb.cuer.app.util.extension.view.fade

/**
 * https://medium.com/@ipaulpro/drag-and-swipe-with-recyclerview-b9456d2b1aaf
 * An implementation of [ItemTouchHelper.Callback] that enables basic drag & drop and
 * swipe-to-dismiss. Drag events are automatically started by an item long-press.<br></br>
 *
 * Expects the `RecyclerView.Adapter` to listen for [ ] callbacks and the `RecyclerView.ViewHolder` to implement
 * [ItemTouchHelperViewHolder].
 *
 * @author Paul Burke (ipaulpro)
 */
class ItemTouchHelperCallback(
    private val interactions: ItemBaseContract.ItemMoveInteractions
) : ItemTouchHelper.Callback() {

    private var swipingLeft: Boolean? = null

    override fun isLongPressDragEnabled(): Boolean {
        return true
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        return true
    }

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        // Set movement flags based on the layout manager
        return if (recyclerView.layoutManager is GridLayoutManager) {
            val dragFlags =
                ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            val swipeFlags = 0
            makeMovementFlags(dragFlags, swipeFlags)
        } else {
            (viewHolder as? ItemTouchHelperViewHolder)
                ?.let {
                    val dragFlags = if (it.canReorder()) ItemTouchHelper.UP or ItemTouchHelper.DOWN else 0
                    var swipeFlags = if (it.canDragLeft()) ItemTouchHelper.START else 0
                    swipeFlags = swipeFlags or if (it.canDragRight()) ItemTouchHelper.END else 0
                    makeMovementFlags(dragFlags, swipeFlags)
                } ?: let {
                val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
                val swipeFlags = ItemTouchHelper.START or ItemTouchHelper.END
                makeMovementFlags(dragFlags, swipeFlags)
            }

        }
    }

    override fun onMove(
        recyclerView: RecyclerView,
        source: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        if (source.itemViewType != target.itemViewType) {
            return false
        }
        return interactions.onItemMove(source.adapterPosition, target.adapterPosition)
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, i: Int) {
        (viewHolder as ItemTouchHelperViewHolder).apply {
            swipingLeft?.let { onItemSwiped(it) }
            swipingLeft = null
        }
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            // Fade out the view as it is swiped out of the parent's bounds
            (viewHolder as? ItemTouchHelperViewHolder)?.apply {
                val alphaValue =
                    ALPHA_FULL - Math.abs(dX) / viewHolder.itemView.width.toFloat()
                contentView.translationX = dX
                contentView.alpha = alphaValue
                val outSideTolerance = Math.abs(dX) > 20
                val left = dX < 0
                if (outSideTolerance) {
                    rightSwipeView?.fade(!left)
                    leftSwipeView?.fade(left)
                    swipingLeft = left
                }
            }
        } else {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
            (viewHolder as? ItemTouchHelperViewHolder)?.apply { onItemSelected() }
        }
        super.onSelectedChanged(viewHolder, actionState)
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        viewHolder.itemView.alpha =
            ALPHA_FULL
        (viewHolder as? ItemTouchHelperViewHolder)?.apply { onItemClear() }
        interactions.onItemClear()
    }

    companion object {
        const val ALPHA_FULL = 1.0f
    }
}