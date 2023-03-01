package uk.co.sentinelweb.cuer.app.ui.browse

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.arkivanov.essenty.lifecycle.asEssentyLifecycle
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
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationProvider
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationRouter
import uk.co.sentinelweb.cuer.app.ui.common.navigation.navigationRouter
import uk.co.sentinelweb.cuer.app.ui.main.MainActivity
import uk.co.sentinelweb.cuer.app.ui.onboarding.OnboardingFragment
import uk.co.sentinelweb.cuer.app.ui.play_control.CompactPlayerScroll
import uk.co.sentinelweb.cuer.app.ui.playlist.PlaylistMviFragment
import uk.co.sentinelweb.cuer.app.ui.search.SearchBottomSheetFragment
import uk.co.sentinelweb.cuer.app.ui.search.SearchBottomSheetFragment.Companion.SEARCH_BOTTOMSHEET_TAG
import uk.co.sentinelweb.cuer.app.usecase.AddBrowsePlaylistUsecase
import uk.co.sentinelweb.cuer.app.util.extension.fragmentScopeWithSource
import uk.co.sentinelweb.cuer.app.util.extension.getFragmentActivity
import uk.co.sentinelweb.cuer.app.util.extension.linkScopeToActivity
import uk.co.sentinelweb.cuer.app.util.wrapper.EdgeToEdgeWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.SnackbarWrapper
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class BrowseFragment : Fragment(), AndroidScopeComponent {

    override val scope: Scope by fragmentScopeWithSource<BrowseFragment>()
    private val controller: BrowseController by inject()
    private val log: LogWrapper by inject()
    private val coroutines: CoroutineContextProvider by inject()
    private val browseMviView: BrowseMviViewProxy by inject()
    private val snackbarWrapper: SnackbarWrapper by inject()
    private val navRouter: NavigationRouter by inject()
    private val edgeToEdgeWrapper: EdgeToEdgeWrapper by inject()
    private val navigationProvider: NavigationProvider by inject()
    private val compactPlayerScroll: CompactPlayerScroll by inject()
    private val res: ResourceWrapper by inject()
    private val browseHelpConfig: BrowseHelpConfig by inject()
    private val addBrowsePlaylistUsecase: AddBrowsePlaylistUsecase by inject()

    private var _binding: FragmentComposeBinding? = null
    private val binding get() = _binding ?: throw IllegalStateException("BrowseFragment view not bound")

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
        // todo make a factory to create the controller here move this to onViewCreated see playlistsMviFrag
        controller.onViewCreated(listOf(browseMviView), lifecycle.asEssentyLifecycle())
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
        OnboardingFragment.showIntro(this@BrowseFragment, browseHelpConfig)
        compactPlayerScroll.raisePlayer(this)
    }

    override fun onResume() {
        super.onResume()
        edgeToEdgeWrapper.setDecorFitsSystemWindows(requireActivity())
    }

    override fun onStop() {
        super.onStop()
        //playerView.showPlayer()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun observeLabels() {
        browseMviView.labelObservable().observe(
            this.viewLifecycleOwner,
            object : Observer<Label> {
                override fun onChanged(label: Label) {
                    when (label) {
                        is Error -> snackbarWrapper.makeError(label.message).show()
                        TopReached -> navRouter.navigate(NavigationModel.FINISH)
                        ActionSettings -> navigationProvider.navigate(R.id.navigation_settings_root)
                        ActionSearch -> {
                            SearchBottomSheetFragment()
                                .show(childFragmentManager, SEARCH_BOTTOMSHEET_TAG)
                        }

                        ActionPasteAdd -> {
                            (requireActivity() as? MainActivity)?.checkIntentAndPasteAdd()
                        }

                        ActionHelp -> {
                            OnboardingFragment.showHelp(this@BrowseFragment, browseHelpConfig)
                        }

                        is AddPlaylist -> {
                            lifecycleScope.launch {
                                browseMviView.loading(true)
                                addBrowsePlaylistUsecase.execute(label.cat, label.parentId)
                                    ?.id
                                    ?.apply {
                                        navRouter.navigate(
                                            PlaylistMviFragment.makeNav(
                                                this.id,
                                                play = false,
                                                source = this.source
                                            )
                                        )
                                    }
                                    ?: snackbarWrapper.makeError(res.getString(R.string.browse_add_error, label.cat.title)).show()
                                browseMviView.loading(false)
                            }
//                            startActivity(
//                                ShareActivity.urlIntent(
//                                    requireContext(),
//                                    playlistUrl(
//                                        label.cat.platformId
//                                            ?: throw IllegalArgumentException("Category has no platform ID : ${label.cat} ")
//                                    ),
//                                    label.parentId,
//                                    label.cat
//                                )
//                            )
                        }

                        is OpenLocalPlaylist -> navRouter.navigate(
                            PlaylistMviFragment.makeNav(label.id.id, play = label.play, source = label.id.source)
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
                        lifecycle = get<BrowseFragment>().lifecycle.asEssentyLifecycle(),
                        log = get()
                    )
                }
                scoped {
                    BrowseStoreFactory(
//                        storeFactory = LoggingStoreFactory(DefaultStoreFactory),
                        storeFactory = DefaultStoreFactory(),
                        repository = get(),
                        playlistOrchestrator = get(),
                        playlistStatsOrchestrator = get(),
                        browseStrings = get(),
                        log = get(),
                        prefs = get(),
                        recentCategories = get()
                    )
                }
                scoped<BrowseContract.Strings> { BrowseStrings(get()) }
                scoped { BrowseRepository(BrowseRepositoryJsonLoader(get()), "browse_categories.json") }
                scoped { BrowseModelMapper(get(), get()) }
                scoped { BrowseMviViewProxy(get(), get()) }
                scoped { navigationRouter(true, this.getFragmentActivity()) }
                scoped { BrowseHelpConfig(get()) }
            }
        }
    }

}