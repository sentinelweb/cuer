package uk.co.sentinelweb.cuer.app.ui.browse

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import kotlinx.android.synthetic.main.browse_fragment.*
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.core.scope.Scope
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.util.extension.fragmentScopeWithSource
import uk.co.sentinelweb.cuer.app.util.firebase.FirebaseDefaultImageProvider
import uk.co.sentinelweb.cuer.app.util.firebase.loadFirebaseOrOtherUrl
import uk.co.sentinelweb.cuer.app.util.wrapper.EdgeToEdgeWrapper

// fixme : this is the app:startDestination fragment which causes a double instance and the menus not to work :/
// when implementing here might need to make a dummy app:startDestination and  manually navigate in mainActivity
class BrowseFragment : Fragment(R.layout.browse_fragment),
    BrowseContract.View,
    AndroidScopeComponent {

    override val scope: Scope by fragmentScopeWithSource()
    private val presenter: BrowseContract.Presenter by inject()
    private val imageProvider: FirebaseDefaultImageProvider by inject()
    private val edgeToEdgeWrapper: EdgeToEdgeWrapper by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        browse_toolbar.let {
            (activity as AppCompatActivity).setSupportActionBar(it)
        }
        browse_collapsing_toolbar.title = "Browse"
        Glide.with(requireContext())
            .loadFirebaseOrOtherUrl("gs://cuer-275020.appspot.com/playlist_header/eye-1256701_640.png", imageProvider)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(browse_header_image)
    }

    override fun onResume() {
        super.onResume()
        edgeToEdgeWrapper.setDecorFitsSystemWindows(requireActivity())
    }
}
