package uk.co.sentinelweb.cuer.app.ui.playlist_item_edit

import android.content.DialogInterface
import android.os.Bundle
import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatDialog
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
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
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipCreator
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel.Type.PLAYLIST
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel.Type.PLAYLIST_SELECT
import uk.co.sentinelweb.cuer.app.ui.common.dialog.SelectDialogCreator
import uk.co.sentinelweb.cuer.app.ui.common.dialog.SelectDialogModel
import uk.co.sentinelweb.cuer.app.ui.playlist_edit.PlaylistEditFragment
import uk.co.sentinelweb.cuer.app.util.navigation.NavigationMapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain


class PlaylistItemEditFragment : Fragment(R.layout.playlist_item_edit_fragment) {

    private val viewModel: PlaylistItemEditViewModel by currentScope.inject()
    private val log: LogWrapper by inject()
    private val navMapper: NavigationMapper by currentScope.inject()
    private val chipCreator: ChipCreator by currentScope.inject()
    private val selectDialogCreator: SelectDialogCreator by currentScope.inject()

    private val starMenuItem: MenuItem by lazy { ple_toolbar.menu.findItem(R.id.share_star) }
    private val playMenuItem: MenuItem by lazy { ple_toolbar.menu.findItem(R.id.share_play) }

    private var dialog: AppCompatDialog? = null
    private var createPlaylistDialog: DialogFragment? = null

    init {
        log.tag = "PlaylistItemEditFragment"
    }

    fun setData(media: MediaDomain?) = viewModel.setData(media)

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
                    // only show the menu items for the non-empty state
                    starMenuItem.isVisible = ple_star_fab.isVisible
                    playMenuItem.isVisible = ple_star_fab.isVisible
                } else if (isShow) {
                    isShow = false
                    starMenuItem.isVisible = false
                    playMenuItem.isVisible = false
                }
            }
        })
        observeModel()
        observeNavigation()
        observeDialog()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        /* init */ viewModel
    }

    private fun observeModel() {
        viewModel.getModelObservable().observe(
            this.viewLifecycleOwner,
            object : Observer<PlaylistItemEditModel> {
                override fun onChanged(model: PlaylistItemEditModel) {
                    ple_author_image.isVisible = !model.empty
                    ple_play_button.isVisible = !model.empty
                    ple_title_pos.isVisible = !model.empty
                    ple_duration.isVisible = !model.empty
                    ple_star_fab.isVisible = !model.empty
                    starMenuItem.setVisible(!model.empty)
                    playMenuItem.setVisible(!model.empty)
                    Picasso.get().load(model.imageUrl).into(ple_image)
                    ple_desc.setText(model.description)
                    ple_title.setText(model.title)
                    ple_toolbar.title = model.title

                    if (model.empty) {
                        return
                    }

                    Picasso.get().load(model.imageUrl).into(ple_image)
                    Picasso.get().load(model.channelThumbUrl).into(ple_author_image)
                    ple_chips.removeAllViews() // todo reuse chip views
                    model.chips.forEach { chipModel ->
                        chipCreator.create(chipModel, ple_chips)?.apply {
                            ple_chips.addView(this)
                            when (chipModel.type) {
                                PLAYLIST_SELECT -> {
                                    setOnClickListener {
                                        viewModel.onSelectPlaylistChipClick(
                                            chipModel
                                        )
                                    }
                                }
                                PLAYLIST -> {
                                    setOnCloseIconClickListener {
                                        viewModel.onRemovePlaylist(
                                            chipModel
                                        )
                                    }
                                }
                                else -> Unit
                            }
                        }
                    }
                    ple_pub_date.setText(model.pubDate)
                    ple_duration.setText(model.durationText)
                    model.position?.let { ratio ->
                        ple_title_pos.layoutParams.width = (ratio * ple_title_bg.width).toInt()
                    } ?: ple_title_pos.apply { isVisible = false }
                    ple_pub_date.setText(model.pubDate)
                    ple_author_title.setText(model.channelTitle)
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
        viewModel.getNavigationObservable().observe(this.viewLifecycleOwner,
            object : Observer<NavigationModel> {
                override fun onChanged(nav: NavigationModel) {
                    when (nav.target) {
                        else -> navMapper.map(nav)
                    }
                }
            }
        )
    }

    private fun observeDialog() {
        viewModel.getDialogObservable().observe(this.viewLifecycleOwner,
            object : Observer<SelectDialogModel> {
                override fun onChanged(model: SelectDialogModel) {
                    dialog?.dismiss()
                    createPlaylistDialog?.let {
                        val ft = childFragmentManager.beginTransaction()
                        ft.hide(it)
                        ft.commit()
                    }
                    when (model.type) {
                        SelectDialogModel.Type.PLAYLIST -> dialog =
                            selectDialogCreator.create(model, object :
                                DialogInterface.OnMultiChoiceClickListener {
                                override fun onClick(
                                    p0: DialogInterface?,
                                    which: Int,
                                    checked: Boolean
                                ) {
                                    viewModel.onPlaylistSelected(which, checked)
                                }
                            }).apply {
                                setButton(DialogInterface.BUTTON_POSITIVE, "OK") { d, _ ->
                                    d.dismiss()
                                }
                                setOnDismissListener { viewModel.onPlaylistDialogClose() }
                                show()
                            }

                        SelectDialogModel.Type.PLAYLIST_ADD -> {
                            createPlaylistDialog = PlaylistEditFragment.newInstance(null).apply {
                                listener = object : PlaylistEditFragment.Listener {
                                    override fun onPlaylistCommit(domain: PlaylistDomain?) {
                                        domain?.apply { viewModel.onPlaylistSelected(this) }
                                        this@PlaylistItemEditFragment.childFragmentManager.beginTransaction()
                                            .apply {
                                                createPlaylistDialog?.let { hide(it) }
                                                commit()
                                            }
                                    }
                                }
                            }
                            createPlaylistDialog?.show(
                                childFragmentManager,
                                CREATE_PLAYLIST_TAG
                            ) // fragmentManager!!
                        }
                        else -> Unit
                    }
                }
            }
        )
    }

    suspend fun commitPlaylistItems() {
        viewModel.commitPlaylistItems()
    }

    companion object {

        private val CREATE_PLAYLIST_TAG = "pe_dialog"

        @JvmStatic
        val fragmentModule = module {
            scope(named<PlaylistItemEditFragment>()) {
                viewModel {
                    PlaylistItemEditViewModel(
                        state = get(),
                        modelMapper = get(),
                        playlistRepo = get(),
                        mediaRepo = get()
                    )
                }
                factory { PlaylistItemEditState() }
                factory { PlaylistItemEditModelMapper(get(), get()) }
                factory {
                    NavigationMapper(
                        activity = (getSource() as Fragment).requireActivity(),
                        toastWrapper = get()
                    )
                }
                factory {
                    ChipCreator((getSource() as Fragment).requireActivity())
                }
                factory {
                    SelectDialogCreator((getSource() as Fragment).requireActivity())
                }
            }
        }
    }
}
