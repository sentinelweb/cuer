package uk.co.sentinelweb.cuer.app.ui.common.ribbon

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import uk.co.sentinelweb.cuer.app.databinding.ViewRibbonItemBinding
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper

class RibbonItemView(
    val item: RibbonModel,
    parent: ViewGroup,
) : KoinComponent {

    private val res: ResourceWrapper by inject()

    private var _binding: ViewRibbonItemBinding? = null
    private val binding get() = _binding!!

    var isVisible: Boolean = true
        set(value) {
            binding.root.isVisible = value
            field = value
        }

    init {
        _binding = ViewRibbonItemBinding.inflate(LayoutInflater.from(parent.context), parent, true)
        binding.ribbonItemText.setText(item.text)
        binding.ribbonItemIcon.setImageDrawable(res.getDrawable(item.icon))
    }

    fun setOnClickListener(listener: (RibbonModel) -> Unit) = binding.root.setOnClickListener {
        listener(item)
    }
}
