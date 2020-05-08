package uk.co.sentinelweb.cuer.app.ui.playlist_item_edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.playlist_item_edit_fragment.*
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.domain.MediaDomain

class PlaylistItemEditFragment : Fragment() {

    companion object {
        fun newInstance() = PlaylistItemEditFragment()
    }

    private lateinit var viewModel: PlaylistItemEditViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.playlist_item_edit_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(PlaylistItemEditViewModel::class.java)
        // TODO: Use the ViewModel
    }

    fun setData(media: MediaDomain) {
        (media.image ?: media.thumbNail)?.let {
            Picasso.get().load(it.url).into(ple_image)
        }
        ple_title.setText(media.title)
        ple_desc.setText(media.description)

    }

}
