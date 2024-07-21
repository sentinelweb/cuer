package uk.co.sentinelweb.cuer.app.ui.cast

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity

// fixme memory leak somewhere - see leak canary
class CastDialogLauncher(
    private val activity: FragmentActivity
) : CastContract.CastDialogLauncher {

    private var dialogFragment: DialogFragment? = null
    override fun launchCastDialog() {
        dialogFragment = CastDialogFragment.newInstance()
        dialogFragment?.show(activity.supportFragmentManager, CAST_DIALOG_FRAGMENT_TAG)
    }

    override fun hideCastDialog() {
        dialogFragment?.dismissAllowingStateLoss()
        dialogFragment = null
    }

    companion object {
        val CAST_DIALOG_FRAGMENT_TAG = "CastDialogFragment"
    }
}