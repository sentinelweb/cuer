package uk.co.sentinelweb.cuer.app.ui.playlists.item.header

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.FragmentNavigator
import uk.co.sentinelweb.cuer.app.databinding.ViewItemHeaderBinding
import uk.co.sentinelweb.cuer.app.ui.playlists.item.ItemContract

class HeaderView() : ItemContract.HeaderView {

    private lateinit var _binding: ViewItemHeaderBinding

    override val root: View
        get() = _binding.root


    fun init(parent: ViewGroup) {
        _binding =
            ViewItemHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    }

    override fun setTitle(title: String) {
        _binding.title.text = title
    }

    override fun makeTransitionExtras(): FragmentNavigator.Extras {
        throw IllegalStateException("should not be called")
    }
}
