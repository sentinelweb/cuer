package uk.co.sentinelweb.cuer.app.ui.playlist.item

// todo view binding
import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.appcompat.widget.PopupMenu
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.view_playlist_item.view.*
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.klink.util.extension.fade


class ItemView constructor(c: Context, a: AttributeSet?, def: Int = 0) : FrameLayout(c, a, def),
    ItemContract.View {

    constructor(c: Context, a: AttributeSet?) : this(c, a, 0)

    private lateinit var presenter: ItemContract.Presenter

    val itemView: View
        get() = listitem
    val rightSwipeView: View
        get() = swipe_label_right
    val leftSwipeView: View
        get() = swipe_label_left

    override fun onFinishInflate() {
        super.onFinishInflate()
        listitem.setOnClickListener { presenter.doClick() }
        listitem_overflow_click.setOnClickListener { showContextualMenu() }
    }

    @SuppressLint("RestrictedApi")
    private fun showContextualMenu() {

        val wrapper = ContextThemeWrapper(context, R.style.ContextMenu)
        //val wrapper = context
        val popup = PopupMenu(wrapper, listitem_overflow_click)
        popup.inflate(R.menu.playlist_context)
        popup.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
            override fun onMenuItemClick(item: MenuItem): Boolean {
                when (item.itemId) {
                    R.id.context_play -> presenter.doPlay(false)
                    R.id.context_play_external -> presenter.doPlay(true)
                    R.id.context_channel_external -> presenter.doShowChannel()
                    R.id.context_star -> presenter.doStar()
                    R.id.context_share -> presenter.doShare()
                }
                return true
            }
        })
        MenuPopupHelper(wrapper, popup.menu as MenuBuilder, listitem_overflow_click).apply {
            setForceShowIcon(true)
            show()
        }
    }

    fun resetBackground() {
        swipe_label_right.fade(false)
        swipe_label_left.fade(false)
        listitem.translationX = 0f
        listitem.alpha = 1f
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

    override fun setIconUrl(url: String) {
        Picasso.get().load(url).into(listitem_icon)
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    //init {
    // this is disabled for the moment but if I need the linearLayout version might be best to have it for that
    // swipeToDismissTouchListener()
    //}

//    private fun swipeToDismissTouchListener() {
//        listitem.setOnTouchListener(swipeDismissTouchListener)
//    }
//
//    private val swipeDismissTouchListener = SwipeDismissTouchListener(
//        listitem,
//        object : SwipeDismissTouchListener.DismissCallbacks {
//
//            override fun onDismissCancel() {
//                resetBackground()
//            }
//
//            override fun isSwiping(left: Boolean, offset: Float) {
//                val outSideTolerance =
//                    abs(offset) > resources.getDimension(R.dimen.list_item_swipe_dismiss_toerance)
//                if (outSideTolerance) {
//                    swipe_label_right.fade(!left)
//                    swipe_label_left.fade(left)
//                }
//            }
//
//            override fun canDismiss(): Boolean {
//                return true
//            }
//
//            override fun onDismiss(left: Boolean) {
//                if (left) {
//                    presenter.doLeft()
//                } else {
//                    presenter.doRight()
//                }
//                resetBackground()
//            }
//        })


}