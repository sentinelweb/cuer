package uk.co.sentinelweb.cuer.app.ui.playlist.item

import android.annotation.SuppressLint
import android.content.Context
import android.text.SpannableString
import android.util.AttributeSet
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.ColorRes
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.navigation.fragment.FragmentNavigatorExtras
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.databinding.ViewPlaylistCardBinding
import uk.co.sentinelweb.cuer.app.ui.playlist_item_edit.PlaylistItemEditFragment.Companion.TRANS_IMAGE
import uk.co.sentinelweb.cuer.app.ui.playlist_item_edit.PlaylistItemEditFragment.Companion.TRANS_TITLE
import uk.co.sentinelweb.cuer.app.util.extension.view.fade
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper


class ItemCardView constructor(c: Context, a: AttributeSet?, def: Int = 0) : FrameLayout(c, a, def),
    ItemContract.View, KoinComponent {

    constructor(c: Context, a: AttributeSet?) : this(c, a, 0)

    private lateinit var presenter: ItemContract.Presenter
    private val log: LogWrapper by inject()
    private val res: ResourceWrapper by inject()
    private var popupMenu: PopupMenu? = null

    private var _binding: ViewPlaylistCardBinding? = null
    private val binding get() = _binding ?: throw Exception("ViewPlaylistCardBinding not bound")

    init {
        log.tag(this)
    }

    override fun setPresenter(itemPresenter: ItemContract.Presenter) {
        presenter = itemPresenter
    }

    override val itemView: View
        get() = binding.listitem
    override val rightSwipeView: View
        get() = binding.swipeLabelRight
    override val leftSwipeView: View
        get() = binding.swipeLabelLeft

    fun isViewForId(id: Long): Boolean = presenter.isViewForId(id)

    override fun onFinishInflate() {
        super.onFinishInflate()
        _binding = ViewPlaylistCardBinding.bind(this)
        binding.listitem.setOnClickListener { presenter.doClick() }
        binding.listitemTop.setOnClickListener { presenter.doClick() }
        binding.listitemBottom.setOnClickListener { presenter.doClick() }
        // todo implement author click
//        binding.listitemAuthorImage.setOnClickListener { presenter.doAuthorClick() }
        binding.listitem.setOnClickListener { presenter.doClick() }
        binding.listitemOverflowClick.setOnClickListener { showContextualMenu() }
        binding.listitemIcon.setOnClickListener { presenter.doIconClick() }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        popupMenu?.dismiss()
    }

    override fun onCreateContextMenu(menu: ContextMenu?) {
        super.onCreateContextMenu(menu)
        log.d("onCreateContextMenu")
    }

    @SuppressLint("RestrictedApi")
    private fun showContextualMenu() {
        log.d("showContextualMenu")
        val wrapper = ContextThemeWrapper(context, R.style.ContextMenu)
        popupMenu?.dismiss()
        val popup = PopupMenu(wrapper, binding.listitemOverflowClick)
        popup.inflate(R.menu.playlist_context)
        popup.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
            override fun onMenuItemClick(item: MenuItem): Boolean {
                when (item.itemId) {
                    R.id.playlist_context_view -> presenter.doIconClick()
                    R.id.playlist_context_play -> presenter.doPlay(false)
                    R.id.playlist_context_play_start -> presenter.doPlayStartClick()
                    R.id.playlist_context_play_external -> presenter.doPlay(true)
                    R.id.playlist_context_channel_external -> presenter.doShowChannel()
                    R.id.playlist_context_star -> presenter.doStar()
                    R.id.playlist_context_share -> presenter.doShare()
                    R.id.playlist_context_related -> presenter.doRelated()
                    R.id.playlist_goto_playlist -> presenter.doGotoPlaylist()
                }
                return true
            }
        })
        popup.menu.findItem(R.id.playlist_context_star).setIcon(
            if (presenter.isStarred()) R.drawable.ic_unstarred_black else R.drawable.ic_menu_starred_black
        )
        popup.menu.findItem(R.id.playlist_context_star).setTitle(
            if (presenter.isStarred()) R.string.menu_unstar else R.string.menu_star
        )
        popup.menu.findItem(R.id.playlist_goto_playlist).isVisible = presenter.isCompositePlaylist()
        popup.setOnDismissListener { popupMenu = null }
        MenuPopupHelper(wrapper, popup.menu as MenuBuilder, binding.listitemOverflowClick).apply {
            setForceShowIcon(true)
            show()
        }
        popupMenu = popup
    }

    override fun dismissMenu() {
        popupMenu?.dismiss()
    }

    override fun setShowOverflow(showOverflow: Boolean) {
        binding.listitemOverflowClick.isVisible = showOverflow
        binding.listitemOverflowImg.isVisible = showOverflow
    }

    override fun resetBackground() {
        binding.swipeLabelRight.fade(false)
        binding.swipeLabelLeft.fade(false)
        binding.listitem.translationX = 0f
        binding.listitem.alpha = 1f
    }

    override fun setImageResource(iconRes: Int) {
        binding.listitemIcon.setImageResource(iconRes)
    }

    override fun setCheckedVisible(checked: Boolean) {
        binding.listitemIconCheck.visibility = if (checked) View.VISIBLE else View.GONE
    }

    override fun setTopText(text: SpannableString) {
        binding.listitemTop.setText(text)
        binding.listitemTop.transitionName = text.toString()
    }

    override fun setBottomText(text: SpannableString) {
        binding.listitemBottom.setText(text)
    }

    override fun setImageUrl(url: String) {
        Glide.with(context)
            .load(url)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(binding.listitemIcon)
        binding.listitemIcon.transitionName = url
    }

    override fun setThumbResource(iconRes: Int) {
        // do nothing
    }

    override fun setThumbUrl(url: String) {
        // do nothing
    }

    override fun setAuthorImageResource(iconRes: Int) {
        binding.listitemAuthorImage.setImageResource(iconRes)
    }

    override fun setAuthorImageUrl(url: String) {
        Glide.with(context)
            .load(url)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(binding.listitemAuthorImage)
        // todo transition image?
        // binding.listitemAuthorImage.transitionName = url
    }

    override fun setBackground(@ColorRes backgroundColor: Int) {
        binding.listitem.setBackgroundResource(backgroundColor)
    }

    override fun setDurationBackground(@ColorRes infoTextBackgroundColor: Int) {
        binding.listitemDuration.setBackgroundColor(res.getColor(infoTextBackgroundColor))
    }

    override fun setPlayIcon(icon: Int) {
        binding.listitemPlayIcon.setImageResource(icon)
    }

    override fun setDuration(text: String) {
        binding.listitemDuration.text = text
    }

    override fun setProgress(ratio: Float) {
        binding.listitemProgress.progress = (100 * ratio).toInt()
    }

    override fun showProgress(live: Boolean) {
        binding.listitemProgress.isVisible = live
    }

    override fun makeTransitionExtras() =
        FragmentNavigatorExtras(
            binding.listitemTop to TRANS_TITLE,
            binding.listitemIcon to TRANS_IMAGE
        )
}
