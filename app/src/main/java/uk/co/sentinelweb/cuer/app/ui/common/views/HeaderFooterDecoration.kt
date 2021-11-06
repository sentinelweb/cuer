package uk.co.sentinelweb.cuer.app.ui.common.views

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class HeaderFooterDecoration(private val headerHeight: Int, private val footerHeight: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val adapter = parent.adapter ?: return
        when (parent.getChildAdapterPosition(view)) {
            0 -> outRect.top = headerHeight
            adapter.itemCount - 1 -> outRect.bottom = footerHeight
            else -> outRect.set(0, 0, 0, 0)
        }
    }
}