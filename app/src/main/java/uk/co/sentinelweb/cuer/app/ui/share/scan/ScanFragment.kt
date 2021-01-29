package uk.co.sentinelweb.cuer.app.ui.share.scan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import org.koin.android.scope.currentScope
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.databinding.ScanFragmentBinding
import uk.co.sentinelweb.cuer.app.util.wrapper.SnackbarWrapper

class ScanFragment : Fragment(R.layout.scan_fragment), ScanContract.View {

    override lateinit var listener: ScanContract.Listener

    private val presenter: ScanContract.Presenter by currentScope.inject()
    private val snackbarWrapper: SnackbarWrapper by currentScope.inject()

    private var _binding: ScanFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = ScanFragmentBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun fromShareUrl(uriString: String) {
        binding.scanProgress.isVisible = true
        binding.scanResult.isVisible = false
        presenter.fromShareUrl(uriString)
    }

    override fun showMessage(msg: String) {
        snackbarWrapper.make(msg).show()
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
