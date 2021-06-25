package uk.co.sentinelweb.cuer.app.ui.player_fragment

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.player_fragment.*
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.core.scope.Scope
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.util.extension.fragmentScopeWithSource
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferencesWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.ToastWrapper

class PlayerFragment : Fragment(R.layout.player_fragment),
    PlayerFragmentContract.View,
    AndroidScopeComponent {

    override val scope: Scope by fragmentScopeWithSource()
    private val presenter: PlayerFragmentContract.Presenter by inject()
    private val toastWrapper: ToastWrapper by inject()
    private val prefsWrapper: GeneralPreferencesWrapper by inject()

//    private var listFragment: Fragment? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        player_toolbar.let {
            (activity as AppCompatActivity).setSupportActionBar(it)
        }
//        listFragment = childFragmentManager.findFragmentById(R.id.player_list_fragment)?.apply {
//            arguments = bundleOf(
//                PLAYLIST_ID.name to prefsWrapper.getLong(CURRENT_PLAYLIST_ID)
//            )
//        }

    }
}