package uk.co.sentinelweb.cuer.app.util.chromecast

import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.mediarouter.app.MediaRouteChooserDialogFragment
import androidx.mediarouter.app.MediaRouteDialogFactory
import com.google.android.gms.cast.framework.CastContext

class CastDialogWrapper constructor(
    private val chromeCastWrapper: ChromeCastWrapper
) {
    private val castContext: CastContext
        get() = chromeCastWrapper.getCastContext()

    fun showRouteSelector(fragmentManager: FragmentManager) {
        castContext.mergedSelector.apply {
            val f: MediaRouteChooserDialogFragment = MediaRouteDialogFactory.getDefault().onCreateChooserDialogFragment()
            f.routeSelector = this
            //f.setUseDynamicGroup(false)

            val transaction: FragmentTransaction = fragmentManager.beginTransaction()
            transaction.add(f, CHOOSER_FRAGMENT_TAG)
            transaction.commitAllowingStateLoss()
        }
    }

    companion object {
        // reuses library tags in case media route button is somehow accessed (might not be possible)
        private const val CHOOSER_FRAGMENT_TAG = "android.support.v7.mediarouter:MediaRouteChooserDialogFragment"
        private const val CONTROLLER_FRAGMENT_TAG = "android.support.v7.mediarouter:MediaRouteControllerDialogFragment"
    }
}