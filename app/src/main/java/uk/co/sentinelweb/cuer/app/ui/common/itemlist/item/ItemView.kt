package uk.co.sentinelweb.cuer.app.ui.common.itemlist.item

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
// todo view binding
import kotlinx.android.synthetic.main.view_media_item.view.*
import kotlinx.android.synthetic.main.view_swipe_item.view.*
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.common.itemlist.item.SwipeDismissTouchListener
import uk.co.sentinelweb.klink.util.extension.fade
import kotlin.math.abs


class ItemView constructor(c: Context, a: AttributeSet?) : FrameLayout(c, a), ItemContract.View {

    private lateinit var presenter: ItemContract.Presenter

    init {
        LayoutInflater.from(context).inflate(R.layout.view_swipe_item, this, true)
        link_item.setOnClickListener({
            presenter.doClick()
        })
        swipeToDismissTouchListener()
    }

    private fun swipeToDismissTouchListener() {
        link_item.setOnTouchListener(
            SwipeDismissTouchListener(
                link_item,
                object : SwipeDismissTouchListener.DismissCallbacks {

                    override fun onDismissCancel() {
                        resetBackground()
                    }

                    override fun isSwiping(left: Boolean, offset: Float) {
                        val outSideTolerance = abs(offset) > resources.getDimension(R.dimen.list_item_swipe_dismiss_toerance)
                        if (outSideTolerance) {
                            swipe_label_right.fade(!left)
                            swipe_label_left.fade(left)
                        }
                    }

                    override fun canDismiss(): Boolean {
                        return true
                    }

                    override fun onDismiss(left: Boolean) {
                        if (left) {
                            presenter.doLeft()
                        } else {
                            presenter.doRight()
                        }
                        resetBackground()
                    }
                })
        )
    }

    private fun resetBackground() {
        swipe_label_right.fade(false)
        swipe_label_left.fade(false)
    }

    override fun setIconResource(iconRes: Int) {
        listitem_icon.setImageResource(iconRes)
    }

    override fun setCheckedVisible(checked: Boolean) {
        listitem_icon_check.visibility = if (checked) View.VISIBLE else View.GONE
    }

    override fun setTopText(text: String) {
        listitem_top.setText(text)
    }

    override fun setBottomText(text: String) {
        listitem_bottom.setText(text)
    }

    override fun setPresenter(itemPresenter: ItemContract.Presenter) {
        presenter = itemPresenter
    }
}
