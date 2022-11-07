package uk.co.sentinelweb.cuer.app.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.databinding.FragmentComposeBinding
import uk.co.sentinelweb.cuer.app.util.extension.fragmentScopeWithSource
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class OnboardingFragment : Fragment(), AndroidScopeComponent {

    override val scope: Scope by fragmentScopeWithSource<OnboardingFragment>()
    private val viewModel: OnboardingViewModel by inject()
    private val log: LogWrapper by inject()

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

    companion object {
        val fragmentModule = module {
            scope(named<OnboardingFragment>()) {
                scoped { OnboardingViewModel(get(), get(), get()) }
                scoped { OnboardingContract.State(config = get()) }
                scoped { onboardingConfig }
                scoped { OnboardingMapper() }
            }
        }
    }

}