package uk.co.sentinelweb.cuer.app.ui.playlist.item

// todo view binding
import android.annotation.SuppressLint
import android.content.Context
import android.text.SpannableString
import android.util.AttributeSet
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.ColorRes
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.appcompat.widget.PopupMenu
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import kotlinx.android.synthetic.main.view_playlist_item.view.*
import org.koin.core.KoinComponent
import org.koin.core.inject
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.util.extension.fade
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper


class ItemView constructor(c: Context, a: AttributeSet?, def: Int = 0) : FrameLayout(c, a, def),
    ItemContract.View, KoinComponent {

    constructor(c: Context, a: AttributeSet?) : this(c, a, 0)

    private lateinit var presenter: ItemContract.Presenter
    private val log: LogWrapper by inject()

    init {
        log.tag(this)
    }

    override fun setPresenter(itemPresenter: ItemContract.Presenter) {
        presenter = itemPresenter
    }

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
                    R.id.playlist_context_view -> presenter.doView()
                    R.id.playlist_context_play -> presenter.doPlay(false)
                    R.id.playlist_context_play_start -> presenter.doPlayStartClick()
                    R.id.playlist_context_play_external -> presenter.doPlay(true)
                    R.id.playlist_context_channel_external -> presenter.doShowChannel()
                    R.id.playlist_context_star -> presenter.doStar()
                    R.id.playlist_context_share -> presenter.doShare()
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

    override fun setTopText(text: SpannableString) {
        listitem_top.setText(text)
    }

    override fun setBottomText(text: SpannableString) {
        listitem_bottom.setText(text)
    }

    override fun setIconUrl(url: String) {
        Glide.with(listitem_icon)
            .load(url)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(listitem_icon)
    }

    override fun setBackground(@ColorRes backgroundColor: Int) {
        listitem.setBackgroundResource(backgroundColor)
    }

    override fun setDuration(text: String) {
        listitem_duration.text = text
    }

    override fun setProgress(ratio: Float) {
        listitem_progress.progress = (100 * ratio).toInt()
    }

}
