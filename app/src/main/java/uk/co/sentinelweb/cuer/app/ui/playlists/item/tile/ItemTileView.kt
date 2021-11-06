package uk.co.sentinelweb.cuer.app.ui.playlists.item.tile

import android.annotation.SuppressLint
import android.text.Spannable
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.databinding.ViewPlaylistsItemTileBinding
import uk.co.sentinelweb.cuer.app.ui.playlists.item.ItemContract
import uk.co.sentinelweb.cuer.app.util.firebase.FirebaseImageProvider
import uk.co.sentinelweb.cuer.app.util.firebase.loadFirebaseOrOtherUrl


class ItemTileView() :
    ItemContract.View, KoinComponent {

    private lateinit var _binding: ViewPlaylistsItemTileBinding
    private lateinit var presenter: ItemContract.Presenter
    private val imageProvider: FirebaseImageProvider by inject()

    val root: View
        get() = _binding.root
    val itemView: View
        get() = _binding.listitem
    val rightSwipeView: View?
        get() = null
    val leftSwipeView: View?
        get() = null

    fun init(parent: ViewGroup) {
        _binding =
            ViewPlaylistsItemTileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        _binding.root.setOnClickListener { presenter.doClick() }
        _binding.listitemOverflowClick.setOnClickListener { showContextualMenu() }
//        _binding.listitemIcon.setOnClickListener { presenter.doImageClick() }
    }

    @SuppressLint("RestrictedApi")
    private fun showContextualMenu() {
        val wrapper = ContextThemeWrapper(_binding.root.context, R.style.ContextMenu)
        val popup = PopupMenu(wrapper, _binding.listitemOverflowClick)
        popup.inflate(R.menu.playlists_context)
        popup.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
            override fun onMenuItemClick(item: MenuItem): Boolean {
                when (item.itemId) {
                    R.id.playlists_context_play -> presenter.doPlay(false)
                    R.id.playlists_edit -> presenter.doEdit()
                    R.id.playlists_context_play_external -> presenter.doPlay(true)
                    R.id.playlists_context_star -> presenter.doStar()
                    R.id.playlists_context_share -> presenter.doShare()
                    R.id.playlists_context_merge -> presenter.doMerge()
                }
                return true
            }
        })
        popup.menu.findItem(R.id.playlists_context_play).isVisible = presenter.canPlay()
        popup.menu.findItem(R.id.playlists_context_play_external).isVisible = presenter.canLaunch()
        popup.menu.findItem(R.id.playlists_context_star).isVisible = presenter.canEdit()
        popup.menu.findItem(R.id.playlists_context_star).setIcon(
            if (presenter.isStarred()) R.drawable.ic_unstarred_black else R.drawable.ic_menu_starred_black
        )
        popup.menu.findItem(R.id.playlists_context_star).setTitle(
            if (presenter.isStarred()) R.string.menu_unstar else R.string.menu_star
        )
        popup.menu.findItem(R.id.playlists_context_share).isVisible = presenter.canShare()
        popup.menu.findItem(R.id.playlists_context_merge).isVisible = presenter.canEdit()
        MenuPopupHelper(wrapper, popup.menu as MenuBuilder, _binding.listitemOverflowClick).apply {
            setForceShowIcon(true)
            show()
        }
    }

    fun resetBackground() {
        _binding.listitem.translationX = 0f
        _binding.listitem.alpha = 1f
    }

    override fun setIconResource(iconRes: Int) {
        _binding.listitemIcon.setImageResource(iconRes)
    }

    override fun setCheckedVisible(checked: Boolean) {
        _binding.listitemIconCheck.visibility = if (checked) View.VISIBLE else View.GONE
    }

    override fun setTopText(text: Spannable) {
        _binding.listitemTop.setText(text)
    }

    override fun setBottomText(text: Spannable) {
        _binding.listitemBottom.setText(text)
    }

    override fun setPresenter(itemPresenter: ItemContract.Presenter) {
        presenter = itemPresenter
    }

    override fun setIconUrl(url: String) {
        Glide.with(_binding.listitemIcon.context)
            .loadFirebaseOrOtherUrl(url, imageProvider)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(_binding.listitemIcon)
    }

    override fun showOverflow(showOverflow: Boolean) {
        _binding.listitemOverflowImg.isVisible = showOverflow
        _binding.listitemOverflowClick.isVisible = showOverflow
    }
}
