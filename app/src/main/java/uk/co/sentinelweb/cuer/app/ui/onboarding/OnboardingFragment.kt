package uk.co.sentinelweb.cuer.app.ui.onboarding

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.core.context.GlobalContext.get
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.databinding.FragmentComposeBinding
import uk.co.sentinelweb.cuer.app.ext.deserialiseOnboarding
import uk.co.sentinelweb.cuer.app.ext.serialise
import uk.co.sentinelweb.cuer.app.ui.browse.BrowseFragment
import uk.co.sentinelweb.cuer.app.ui.common.ktx.bindFlow
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.ONBOARD_CONFIG
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.ONBOARD_KEY
import uk.co.sentinelweb.cuer.app.ui.main.MainActivity
import uk.co.sentinelweb.cuer.app.ui.onboarding.OnboardingContract.Label.Finished
import uk.co.sentinelweb.cuer.app.ui.onboarding.OnboardingContract.Label.Skip
import uk.co.sentinelweb.cuer.app.ui.playlist.PlaylistMviFragment
import uk.co.sentinelweb.cuer.app.ui.playlist_edit.PlaylistEditFragment
import uk.co.sentinelweb.cuer.app.ui.playlist_item_edit.PlaylistItemEditFragment
import uk.co.sentinelweb.cuer.app.ui.playlists.PlaylistsMviFragment
import uk.co.sentinelweb.cuer.app.util.extension.fragmentScopeWithSource
import uk.co.sentinelweb.cuer.app.util.extension.linkScopeToActivity
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferences.ONBOARDED_PREFIX
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferencesWrapper
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class OnboardingFragment : DialogFragment(), AndroidScopeComponent {

    override val scope: Scope by fragmentScopeWithSource<OnboardingFragment>()
    private val viewModel: OnboardingViewModel by inject()
    private val log: LogWrapper by inject()
    private val contextProvider: CoroutineContextProvider by inject()
    private val multiPlatformPreferences: MultiPlatformPreferencesWrapper by inject()
    private val config: OnboardingContract.Config by inject()

    private var _binding: FragmentComposeBinding? = null
    private val binding get() = _binding ?: throw IllegalStateException("BrowseFragment view not bound")

    private val shownPrefKey: String
        get() = arguments?.getString(ONBOARD_KEY.toString())
            ?: throw IllegalArgumentException("No key")

    init {
        log.tag(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentComposeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        precacheBackgrounds()
        binding.composeView.setContent {
            OnboardingComposables.OnboardingUi(viewModel)
        }
    }

    private fun precacheBackgrounds() {
        lifecycleScope.launch {
            config.screens.map { it.backgroundUrl }.forEach { url ->
                Glide.with(this@OnboardingFragment)
                    .asBitmap()
                    .load(url)
                    .preload();
            }
        }
    }

    private fun observeLabel(label: OnboardingContract.Label) = when (label) {
        Finished -> closeDismiss(label)
        Skip -> closeDismiss(label)
    }

    private fun closeDismiss(label: OnboardingContract.Label) {
        multiPlatformPreferences.putBoolean(ONBOARDED_PREFIX, shownPrefKey, true)
        dismiss()
        if (shownPrefKey == MainActivity::class.simpleName) {
            (requireActivity() as? MainActivity)?.finishedOnboarding()
            if (label == Skip) {
                setOnboardingState(isShown = true)
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        linkScopeToActivity()
    }

    override fun setArguments(args: Bundle?) {
        super.setArguments(args)
        bindFlow(viewModel.label, ::observeLabel)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        contextProvider.mainScope.cancel()
    }

    companion object {
        const val TAG = "OnboardingFragment"
        fun showIntro(f: Fragment, config: OnboardingContract.ConfigBuilder) {
            if (shouldShowIntro(f).not()) {
                OnboardingFragment()
                    .apply {
                        arguments = bundleOf(
                            ONBOARD_CONFIG.toString() to config.build().serialise(),
                            ONBOARD_KEY.toString() to f::class.simpleName!!
                        )
                    }
                    .show(f.requireActivity().supportFragmentManager, TAG)
            }
        }

        private fun shouldShowIntro(f: Fragment): Boolean {
            val multiPlatformPreferences: MultiPlatformPreferencesWrapper = get().get()
            return multiPlatformPreferences.getBoolean(ONBOARDED_PREFIX, f::class.simpleName!!, false)
        }

        fun setOnboardingState(isShown: Boolean) {
            val multiPlatformPreferences: MultiPlatformPreferencesWrapper = get().get()
            multiPlatformPreferences.putBoolean(ONBOARDED_PREFIX, MainActivity::class.simpleName!!, isShown)
            multiPlatformPreferences.putBoolean(ONBOARDED_PREFIX, PlaylistMviFragment::class.simpleName!!, isShown)
            multiPlatformPreferences.putBoolean(ONBOARDED_PREFIX, PlaylistsMviFragment::class.simpleName!!, isShown)
            multiPlatformPreferences.putBoolean(ONBOARDED_PREFIX, PlaylistEditFragment::class.simpleName!!, isShown)
            multiPlatformPreferences.putBoolean(ONBOARDED_PREFIX, PlaylistItemEditFragment::class.simpleName!!, isShown)
            multiPlatformPreferences.putBoolean(ONBOARDED_PREFIX, BrowseFragment::class.simpleName!!, isShown)

        }

        fun showHelp(f: Fragment, config: OnboardingContract.ConfigBuilder) {
            OnboardingFragment()
                .apply {
                    arguments = bundleOf(
                        ONBOARD_CONFIG.toString() to config.build().serialise(),
                        ONBOARD_KEY.toString() to f::class.simpleName!!
                    )
                }
                .show(f.requireActivity().supportFragmentManager, TAG)
        }

        val fragmentModule = module {
            scope(named<OnboardingFragment>()) {
                scoped { OnboardingViewModel(get(), get(), get(), get(), get()) }
                scoped { OnboardingContract.State(config = get()) }
                scoped {
                    val arguments = get<OnboardingFragment>().arguments
                    arguments
                        ?.getString(ONBOARD_CONFIG.toString())
                        ?.let { deserialiseOnboarding(it) }
                        ?: throw IllegalArgumentException("No onboarding config")
                }
                scoped { OnboardingMapper() }
                scoped { CoroutineContextProvider() }//todo wtf why
            }
        }
    }
}