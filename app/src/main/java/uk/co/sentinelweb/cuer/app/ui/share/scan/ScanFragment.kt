package uk.co.sentinelweb.cuer.app.ui.share.scan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.core.scope.Scope
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.databinding.FragmentScanBinding
import uk.co.sentinelweb.cuer.app.ui.common.views.PlayYangProgress
import uk.co.sentinelweb.cuer.app.util.extension.fragmentScopeWithSource
import uk.co.sentinelweb.cuer.app.util.wrapper.SnackbarWrapper

class ScanFragment : Fragment(R.layout.fragment_scan), ScanContract.View, AndroidScopeComponent {

    override val scope: Scope by fragmentScopeWithSource<ScanFragment>()
    override lateinit var listener: ScanContract.Listener

    private val presenter: ScanContract.Presenter by inject()
    private val snackbarWrapper: SnackbarWrapper by inject()
    private val playYangProgress: PlayYangProgress by inject()

    private var _binding: FragmentScanBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentScanBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        playYangProgress.init(binding.scanProgress, R.color.white)
    }

    override fun fromShareUrl(uriString: String) {// called from activity
        binding.scanProgress.isVisible = true
        binding.scanResult.isVisible = false
        presenter.fromShareUrl(uriString)
    }

    override fun showMessage(msg: String) {
        snackbarWrapper.make(msg).show()
    }

    override fun showError(msg: String) {
        snackbarWrapper.makeError(msg).show()
    }

    override fun setModel(model: ScanContract.Model) {
        binding.scanResult.setImageResource(model.resultIcon)
        binding.scanText.isVisible = true
        binding.scanText.text = model.text
        binding.scanProgress.isVisible = model.isLoading
        binding.scanResult.isVisible = !model.isLoading
    }

    override fun setResult(result: ScanContract.Result) {
        listener.scanResult(result)
    }

}
