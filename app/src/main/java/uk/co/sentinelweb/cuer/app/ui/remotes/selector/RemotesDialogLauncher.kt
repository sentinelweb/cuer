package uk.co.sentinelweb.cuer.app.ui.remotes.selector

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import uk.co.sentinelweb.cuer.domain.RemoteNodeDomain

// fixme memory leak somewhere - see leak canary
class RemotesDialogLauncher(
    private val activity: FragmentActivity
) : RemotesDialogContract.Launcher {

    private var dialogFragment: DialogFragment? = null
    override fun launchRemotesDialog(selected: (RemoteNodeDomain) -> Unit) {
        dialogFragment = RemotesDialogFragment.newInstance(selected)
        dialogFragment?.show(activity.supportFragmentManager, CAST_DIALOG_FRAGMENT_TAG)
    }

    override fun hideRemotesDialog() {
        dialogFragment?.dismissAllowingStateLoss()
        dialogFragment = null
    }

    companion object {
        val CAST_DIALOG_FRAGMENT_TAG = "RemotesDialogFragment"
    }
}