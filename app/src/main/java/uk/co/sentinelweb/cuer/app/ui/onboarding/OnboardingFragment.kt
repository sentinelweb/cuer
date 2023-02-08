package uk.co.sentinelweb.cuer.app.ui.onboarding

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.cancel
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.databinding.FragmentComposeBinding
import uk.co.sentinelweb.cuer.app.ext.deserialiseOnboarding
import uk.co.sentinelweb.cuer.app.ext.serialise
import uk.co.sentinelweb.cuer.app.ui.common.ktx.bindFlow
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.ONBOARD_CONFIG
import uk.co.sentinelweb.cuer.app.util.extension.fragmentScopeWithSource
import uk.co.sentinelweb.cuer.app.util.extension.linkScopeToActivity
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class OnboardingFragment : DialogFragment(), AndroidScopeComponent {

    override val scope: Scope by fragmentScopeWithSource<OnboardingFragment>()
    private val viewModel: OnboardingViewModel by inject()
    private val log: LogWrapper by inject()
    private val contextProvider: CoroutineContextProvider by inject()

    private var _binding: FragmentComposeBinding? = null
    private val binding get() = _binding ?: throw IllegalStateException("BrowseFragment view not bound")

    init {
        log.tag(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentComposeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.composeView.setContent {
            OnboardingComposables.OnboardingUi(viewModel)
        }
    }

    private fun observeLabel(label: OnboardingContract.Label) = when (label) {
        OnboardingContract.Label.Finished -> dismiss()
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

        fun show(a: FragmentActivity, config: OnboardingContract.ConfigBuilder) {
            OnboardingFragment()
                .apply { arguments = bundleOf(ONBOARD_CONFIG.toString() to config.build().serialise()) }
                .show(a.supportFragmentManager, TAG)
        }

        val fragmentModule = module {
            scope(named<OnboardingFragment>()) {
                scoped { OnboardingViewModel(get(), get(), get(), get(), get()) }
                scoped { OnboardingContract.State(config = get()) }
                scoped {
                    get<OnboardingFragment>()
                        .arguments
                        ?.getString(ONBOARD_CONFIG.toString())
                        ?.let { deserialiseOnboarding(it) }
                        ?: AppInstallHelpConfig(get()).build()
                        ?: throw IllegalArgumentException("Could not load onboarding config")
                }
                scoped { OnboardingMapper() }
                scoped { CoroutineContextProvider() }//todo wtf why
            }
        }
    }
}