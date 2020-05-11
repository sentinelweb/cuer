package uk.co.sentinelweb.cuer.app.ui.playlist_item_edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
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


class PlaylistItemEditFragment : Fragment() {

    private val viewModel: PlaylistItemEditViewModel by currentScope.inject()
    private val log: LogWrapper by inject()

    private val starMenuItem: MenuItem by lazy { ple_toolbar.menu.findItem(R.id.star) }

    init {
        log.tag = "PlaylistItemEditFragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.playlist_item_edit_fragment, container, false)
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
        starMenuItem.isVisible = false
        ple_toolbar.title = media.title
        ple_appbar.addOnOffsetChangedListener(object : AppBarLayout.OnOffsetChangedListener {

            var isShow = false
            var scrollRange = -1

            override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange()
                }
                log.d("scrollRange: $scrollRange :: verticalOffset:$verticalOffset :: isShow:$isShow")
                if (scrollRange + verticalOffset == 0) {
                    isShow = true
                    //ple_toolbar.title = media.title
                    starMenuItem.isVisible = true
                } else if (isShow) {
                    isShow = false
                    //ple_toolbar.title = "--"
                    starMenuItem.isVisible = false
                }
            }
        })

        ple_toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.star -> {
                    // Handle favorite icon press
                    true
                }
                else -> false
            }
        }
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
