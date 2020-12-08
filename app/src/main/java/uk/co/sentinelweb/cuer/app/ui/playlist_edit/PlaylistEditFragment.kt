package uk.co.sentinelweb.cuer.app.ui.playlist_edit

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.playlist_edit_fragment.*
import org.koin.android.ext.android.inject
import org.koin.android.scope.currentScope
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel
import uk.co.sentinelweb.cuer.app.util.firebase.FirebaseDefaultImageProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.PlaylistDomain


class PlaylistEditFragment constructor(private val id: Long? = null) : DialogFragment() {

    private val viewModel: PlaylistEditViewModel by currentScope.inject()
    private val log: LogWrapper by inject()
    private val imageProvider: FirebaseDefaultImageProvider by inject()

    private val starMenuItem: MenuItem by lazy { pe_toolbar.menu.findItem(R.id.share_star) }

    private var lastImageUrl: String? = null

    internal var listener: Listener? = null

    interface Listener {
        fun onPlaylistCommit(domain: PlaylistDomain?)
    }

    init {
        log.tag = "PlaylistEditFragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.playlist_edit_fragment, parent, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pe_star_fab.setOnClickListener { viewModel.onStarClick() }
        starMenuItem.isVisible = false
        pe_toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.share_star -> {
                    viewModel.onStarClick()
                    true
                }
                else -> false
            }
        }

        pe_image.setOnTouchListener @SuppressLint("ClickableViewAccessibility") { iv, e ->
            when (e.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    viewModel.onImageClick(e.x > iv.width / 2)
                    true
                }
                else -> true
            }
        }
        pe_commit_button.setOnClickListener { viewModel.onCommitClick() }
        pe_title_edit.doAfterTextChanged { text -> viewModel.onTitleChanged(text.toString()) }
        pe_appbar.addOnOffsetChangedListener(object : AppBarLayout.OnOffsetChangedListener {

            var isShow = false
            var scrollRange = -1

            override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange()
                }
                if (scrollRange + verticalOffset == 0) {
                    isShow = true
                    // only show the menu items for the non-empty state
                    starMenuItem.isVisible = pe_star_fab.isVisible
                } else if (isShow) {
                    isShow = false
                    starMenuItem.isVisible = false
                }
            }
        })
        observeModel()
        observeDomain()
    }

    override fun onResume() {
        super.onResume()

        arguments?.getLong(NavigationModel.Param.PLAYLIST_ID.toString())?.also {
            viewModel.setData(it)
        } ?: run { id?.let { viewModel.setData(it) } }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        /* init */ viewModel
    }

    private fun observeModel() {
        viewModel.getModelObservable().observe(
            this.viewLifecycleOwner,
            object : Observer<PlaylistEditModel> {
                override fun onChanged(model: PlaylistEditModel) {
                    if (pe_title_edit.text.toString() != model.titleEdit) {
                        pe_title_edit.setText(model.titleEdit)
                        pe_title_edit.setSelection(model.titleEdit.length)
                    }
                    pe_toolbar.title = model.titleDisplay

                    val starIconResource =
                        if (model.starred) R.drawable.ic_button_starred_white
                        else R.drawable.ic_button_unstarred_white
                    starMenuItem.setIcon(starIconResource)
                    pe_star_fab.setImageResource(starIconResource)
                    model.button?.apply { pe_commit_button.text = this }
                    model.imageUrl?.let {
                        if (lastImageUrl != it) {
                            Glide.with(pe_image.context)
                                .load(imageProvider.makeRef(it))
                                .into(pe_image)
                        }
                    }
                    lastImageUrl = model.imageUrl
                    model.validation?.apply {
                        pe_commit_button.isEnabled = valid
                        if (!valid) {
                            fieldValidations.forEach {
                                when (it.field) {
                                    PlaylistValidator.PlaylistField.TITLE ->
                                        pe_title.error = getString(it.error)
                                }
                            }
                        } else {
                            pe_title.error = null
                        }
                    }
                }
            })
    }

    private fun observeDomain() {
        viewModel.getDomainObservable().observe(
            this.viewLifecycleOwner,
            object : Observer<PlaylistDomain> {
                override fun onChanged(domain: PlaylistDomain) {
                    listener?.onPlaylistCommit(domain)
                        ?: findNavController().popBackStack()
                }
            })
    }

    companion object {

        fun newInstance(id: Long?): PlaylistEditFragment {
            return PlaylistEditFragment(id)
        }

        @JvmStatic
        val fragmentModule = module {
            scope(named<PlaylistEditFragment>()) {
                viewModel {
                    PlaylistEditViewModel(
                        state = get(),
                        mapper = get(),
                        playlistRepo = get(),
                        log = get(),
                        imageProvider = get()
                    )
                }
                factory { PlaylistEditState() }
                factory {
                    PlaylistEditModelMapper(
                        res = get(),
                        validator = get()
                    )
                }
                factory { PlaylistValidator(get()) }
            }
        }
    }
}
