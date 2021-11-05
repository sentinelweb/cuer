package uk.co.sentinelweb.cuer.app.ui.playlists.item.header

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import uk.co.sentinelweb.cuer.app.databinding.ViewItemHeaderBinding

class HeaderView()  {

    private lateinit var _binding: ViewItemHeaderBinding

    val root: View
        get() = _binding.root


    fun init(parent: ViewGroup) {
        _binding =
            ViewItemHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    }

    fun setTitle(title:String) {
        _binding.title.text = title
    }
}
