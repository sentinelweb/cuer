package uk.co.sentinelweb.cuer.app.ui.play_control

import android.view.View
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView

class CompactPlayerScroll {

    interface PlayerHost {
        fun raisePlayer()
        fun lowerPlayer()
    }

    fun addScrollListener(recyclerView: RecyclerView, f: Fragment) {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (0 > dy) {
                    (f.requireActivity() as? PlayerHost)?.lowerPlayer()
                } else if (0 < dy) {
                    (f.requireActivity() as? PlayerHost)?.raisePlayer()
                }
            }

        })
    }

    fun addScrollListener(recyclerView: NestedScrollView, f: Fragment) {
        recyclerView.setOnScrollChangeListener(object : View.OnScrollChangeListener {

            override fun onScrollChange(
                v: View?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int
            ) {
                if (scrollY < oldScrollY) {
                    (f.requireActivity() as? PlayerHost)?.lowerPlayer()
                } else if (scrollY > oldScrollY) {
                    (f.requireActivity() as? PlayerHost)?.raisePlayer()
                }
            }

        })
    }
}