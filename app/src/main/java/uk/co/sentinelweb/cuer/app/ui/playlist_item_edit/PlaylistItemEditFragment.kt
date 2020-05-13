package uk.co.sentinelweb.cuer.app.ui.playlist_item_edit

import android.os.Bundle
import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.android.material.appbar.AppBarLayout
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.playlist_item_edit_fragment.*
import org.koin.android.ext.android.inject
import org.koin.android.scope.currentScope
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.common.NavigationModel
import uk.co.sentinelweb.cuer.app.util.navigation.NavigationMapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.MediaDomain


class PlaylistItemEditFragment : Fragment(R.layout.playlist_item_edit_fragment) {

    private val viewModel: PlaylistItemEditViewModel by currentScope.inject()
    private val log: LogWrapper by inject()
    private val navMapper: NavigationMapper by inject()

    private val starMenuItem: MenuItem by lazy { ple_toolbar.menu.findItem(R.id.share_star) }
    private val playMenuItem: MenuItem by lazy { ple_toolbar.menu.findItem(R.id.share_play) }

    init {
        log.tag = "PlaylistItemEditFragment"
    }

    fun setData(media: MediaDomain) = viewModel.setData(media)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ple_play_button.setOnClickListener { viewModel.onPlayVideoLocal() }
        ple_star_fab.setOnClickListener { viewModel.onStarClick() }
        starMenuItem.isVisible = false
        playMenuItem.isVisible = false
        ple_desc.setMovementMethod(object : LinkMovementMethod() {
            override fun handleMovementKey(
                widget: TextView?,
                buffer: Spannable?,
                keyCode: Int,
                movementMetaState: Int,
                event: KeyEvent?
            ): Boolean {
                buffer?.run { viewModel.onLinkClick(this.toString()) }
                return true
            }
        })
        ple_toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.share_star -> {
                    viewModel.onStarClick()
                    true
                }
                R.id.share_play -> {
                    viewModel.onPlayVideoLocal()
                    true
                }
                else -> false
            }
        }
        ple_appbar.addOnOffsetChangedListener(object : AppBarLayout.OnOffsetChangedListener {

            var isShow = false
            var scrollRange = -1

            override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange()
                }
                if (scrollRange + verticalOffset == 0) {
                    isShow = true
                    starMenuItem.isVisible = true
                    playMenuItem.isVisible = true
                } else if (isShow) {
                    isShow = false
                    starMenuItem.isVisible = false
                    playMenuItem.isVisible = false
                }
            }
        })
        observeModel()
        observeNavigation()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        /* init */ viewModel
    }

    private fun observeModel() {
        viewModel.getModelObservable()
            .observe(this.viewLifecycleOwner, object : Observer<PlaylistItemEditModel> {
                override fun onChanged(model: PlaylistItemEditModel) {
                    Picasso.get().load(model.imageUrl).into(ple_image)
                    Picasso.get().load(model.channelThumbUrl).into(ple_author_image)
                    ple_title.setText(model.title)
                    ple_author_title.setText(model.channelTitle)
                    ple_desc.setText(model.description)
                    ple_toolbar.title = model.title
                    ple_play_button.isVisible = model.canPlay
                    val starIconResource =
                        if (model.starred) R.drawable.ic_button_starred_white
                        else R.drawable.ic_button_unstarred_white
                    starMenuItem.setIcon(starIconResource)
                    ple_star_fab.setImageResource(starIconResource)
                }
            })
    }

    private fun observeNavigation() {
        viewModel.getNavigationObservable()
            .observe(this.viewLifecycleOwner,
                object : Observer<NavigationModel> {
                    override fun onChanged(nav: NavigationModel) {
                        navMapper.map(nav)
                    }
                }
            )
    }

    companion object {
        @JvmStatic
        val fragmentModule = module {
            scope(named<PlaylistItemEditFragment>()) {
                viewModel { PlaylistItemEditViewModel(get(), get()) }
                factory { PlaylistItemEditState() }
                factory { PlaylistItemEditModelMapper() }
                factory {
                    NavigationMapper(
                        activity = (getSource() as Fragment).requireActivity(),
                        toastWrapper = get()
                    )
                }
            }
        }
    }
}
