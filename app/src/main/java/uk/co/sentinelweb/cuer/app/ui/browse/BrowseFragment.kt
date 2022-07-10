package uk.co.sentinelweb.cuer.app.ui.browse

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.arkivanov.mvikotlin.core.lifecycle.asMviLifecycle
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.databinding.FragmentComposeBinding
import uk.co.sentinelweb.cuer.app.ui.browse.BrowseContract.MviStore.Label
import uk.co.sentinelweb.cuer.app.ui.browse.BrowseContract.MviStore.Label.*
import uk.co.sentinelweb.cuer.app.ui.browse.BrowseContract.View.Event.OnResume
import uk.co.sentinelweb.cuer.app.ui.browse.BrowseContract.View.Event.OnUpClicked
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationMapper
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationProvider
import uk.co.sentinelweb.cuer.app.ui.common.navigation.navigationMapper
import uk.co.sentinelweb.cuer.app.ui.main.MainContract
import uk.co.sentinelweb.cuer.app.ui.playlist.PlaylistContract
import uk.co.sentinelweb.cuer.app.ui.search.SearchBottomSheetFragment
import uk.co.sentinelweb.cuer.app.ui.search.SearchBottomSheetFragment.Companion.SEARCH_BOTTOMSHEET_TAG
import uk.co.sentinelweb.cuer.app.ui.share.ShareActivity
import uk.co.sentinelweb.cuer.app.util.extension.fragmentScopeWithSource
import uk.co.sentinelweb.cuer.app.util.extension.getFragmentActivity
import uk.co.sentinelweb.cuer.app.util.extension.linkScopeToActivity
import uk.co.sentinelweb.cuer.app.util.wrapper.EdgeToEdgeWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.SnackbarWrapper
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.platform.YoutubeUrl.Companion.playlistUrl

class BrowseFragment constructor() : Fragment(), AndroidScopeComponent {

    override val scope: Scope by fragmentScopeWithSource()
    private val controller: BrowseController by inject()
    private val log: LogWrapper by inject()
    private val coroutines: CoroutineContextProvider by inject()
    private val browseMviView: BrowseMviView by inject()
    private val playerView: MainContract.PlayerViewControl by inject()
    private val snackbarWrapper: SnackbarWrapper by inject()
    private val navMapper: NavigationMapper by inject()
    private val edgeToEdgeWrapper: EdgeToEdgeWrapper by inject()
    private val navigationProvider: NavigationProvider by inject()

    private var _binding: FragmentComposeBinding? = null
    private val binding get() = _binding!!

    init {
        log.tag(this)
    }

    // saves the data on back press (enabled in onResume)
    private val upCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            browseMviView.dispatch(OnUpClicked)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        controller.onViewCreated(listOf(browseMviView), lifecycle.asMviLifecycle())

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentComposeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.composeView.setContent {
            BrowseComposables.BrowseUi(browseMviView)
        }
        coroutines.mainScope.launch {
            delay(300)
            browseMviView.dispatch(OnResume)
        }
        observeLabels()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        requireActivity().onBackPressedDispatcher.addCallback(this, upCallback)
        linkScopeToActivity()
    }

    override fun onStart() {
        super.onStart()
        playerView.hidePlayer()
    }

    override fun onResume() {
        super.onResume()
        edgeToEdgeWrapper.setDecorFitsSystemWindows(requireActivity())
    }

    override fun onStop() {
        super.onStop()
        playerView.showPlayer()
    }

    private fun observeLabels() {
        browseMviView.labelObservable().observe(
            this.viewLifecycleOwner,
            object : Observer<Label> {
                override fun onChanged(label: Label) {
                    when (label) {
                        is Error -> snackbarWrapper.makeError(label.message).show()
                        TopReached -> navMapper.navigate(NavigationModel.FINISH)
                        ActionSettings -> navigationProvider.navigate(R.id.navigation_settings_root)
                        ActionSearch -> {
                            SearchBottomSheetFragment()
                                .show(childFragmentManager, SEARCH_BOTTOMSHEET_TAG)
                        }
                        is AddPlaylist -> {
                            startActivity(
                                ShareActivity.urlIntent(
                                    requireContext(),
                                    playlistUrl(
                                        label.cat.platformId
                                            ?: throw IllegalArgumentException("Category has no platform ID : ${label.cat} ")
                                    ),
                                    label.parentId,
                                    label.cat
                                )
                            )
                        }
                        is OpenLocalPlaylist -> navMapper.navigate(
                            PlaylistContract.makeNav(
                                label.id,
                                play = label.play
                            )
                        )
                        None -> Unit
                    }
                }
            })
    }

    class BrowseStrings(private val res: ResourceWrapper) : BrowseContract.Strings {
        override val allCatsTitle: String
            get() = res.getString(R.string.browse_all_cats_title)
        override val recent: String
            get() = res.getString(R.string.browse_recent)
        override val errorNoPlaylistConfigured = res.getString(R.string.browse_error_no_playlist)
        override fun errorNoCatWithID(id: Long) =
            res.getString(R.string.browse_error_no_category, id)
    }

    companion object {
        @JvmStatic
        val fragmentModule = module {
            scope(named<BrowseFragment>()) {
                scoped {
                    BrowseController(
                        storeFactory = get(),
                        modelMapper = get(),
                        lifecycle = getSource<BrowseFragment>().lifecycle.asMviLifecycle(),
                        log = get()
                    )
                }
                scoped {
                    BrowseStoreFactory(
//                        storeFactory = LoggingStoreFactory(DefaultStoreFactory),
                        storeFactory = DefaultStoreFactory,
                        repository = get(),
                        playlistOrchestrator = get(),
                        browseStrings = get(),
                        log = get(),
                        prefs = get(),
                        recentCategories = get()
                    )
                }
                scoped<BrowseContract.Strings> { BrowseStrings(get()) }
                scoped { BrowseRepository(BrowseJsonLoader(get())) }
                scoped { BrowseModelMapper(get(), get()) }
                scoped { BrowseMviView(get(), get()) }
                scoped { navigationMapper(true, this.getFragmentActivity()) }
            }
        }
    }

}