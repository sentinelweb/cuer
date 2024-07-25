package uk.co.sentinelweb.cuer.app.ui.cast

import android.app.Activity
import android.app.Application
import android.os.Build
import android.os.Bundle
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
        registerCleanupOnDestroy()
    }

    private fun registerCleanupOnDestroy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            activity.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
                override fun onActivityDestroyed(p0: Activity) = hideCastDialog()

                override fun onActivityCreated(p0: Activity, p1: Bundle?) = Unit
                override fun onActivityStarted(p0: Activity) = Unit
                override fun onActivityResumed(p0: Activity) = Unit
                override fun onActivityPaused(p0: Activity) = Unit
                override fun onActivityStopped(p0: Activity) = Unit
                override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) = Unit
            })
        }
    }

    override fun hideCastDialog() {
        dialogFragment?.dismissAllowingStateLoss()
        dialogFragment = null
    }


    companion object {
        val CAST_DIALOG_FRAGMENT_TAG = "CastDialogFragment"
    }
}