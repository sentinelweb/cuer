package uk.co.sentinelweb.cuer.app.ui.playlist_item_edit

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.AppBarLayout
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.playlist_item_edit_fragment.*
import org.koin.android.ext.android.inject
import org.koin.android.scope.currentScope
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.util.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.MediaDomain


class PlaylistItemEditFragment : Fragment(R.layout.playlist_item_edit_fragment) {

    private val viewModel: PlaylistItemEditViewModel by currentScope.inject()
    private val log: LogWrapper by inject()

    private val starMenuItem: MenuItem by lazy { ple_toolbar.menu.findItem(R.id.share_star) }
    private val playMenuItem: MenuItem by lazy { ple_toolbar.menu.findItem(R.id.share_play) }


    init {
        log.tag = "PlaylistItemEditFragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ple_play_button.setOnClickListener { log.d("play clicked") }
        starMenuItem.isVisible = false
        playMenuItem.isVisible = false
        ple_toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.share_star -> {
                    // Handle favorite icon press
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
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        /* init */ viewModel
    }

    fun setData(media: MediaDomain) {
        (media.image ?: media.thumbNail)?.let {
            Picasso.get().load(it.url).into(ple_image)
        }
        ple_title.setText(media.title)
        ple_desc.setText(media.description)
        ple_toolbar.title = media.title
    }

    companion object {
        @JvmStatic
        val fragmentModule = module {
            scope(named<PlaylistItemEditFragment>()) {
                viewModel { PlaylistItemEditViewModel() }
            }
        }
    }
}
