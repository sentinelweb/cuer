package uk.co.sentinelweb.cuer.app.util.chromecast

import android.content.Context
import com.google.android.gms.cast.framework.CastOptions
import com.google.android.gms.cast.framework.OptionsProvider
import com.google.android.gms.cast.framework.SessionProvider
import com.google.android.gms.cast.framework.media.CastMediaOptions
import com.google.android.gms.cast.framework.media.MediaIntentReceiver
import com.google.android.gms.cast.framework.media.NotificationOptions
import uk.co.sentinelweb.cuer.app.ui.main.MainActivity

// used by manifest
class CastOptionsProvider : OptionsProvider {

    override fun getCastOptions(appContext: Context): CastOptions {

        val receiverId = RECEIVER_ID
        val buttonActions: MutableList<String> = ArrayList()

        buttonActions.add(MediaIntentReceiver.ACTION_TOGGLE_PLAYBACK)
        buttonActions.add(MediaIntentReceiver.ACTION_STOP_CASTING)

        val compatButtonActionsIndicies = intArrayOf(0, 1)

        val notificationOptions = NotificationOptions.Builder()
            .setActions(buttonActions, compatButtonActionsIndicies)
            .setTargetActivityClassName(MainActivity::class.java.getName())
            .build()
        val mediaOptions = CastMediaOptions.Builder()
            .setNotificationOptions(notificationOptions)
            .setExpandedControllerActivityClassName(MainActivity::class.java.getName())
            .build()
        return CastOptions.Builder()
            .setReceiverApplicationId(receiverId)
            .setCastMediaOptions(mediaOptions)
            .build()
    }


    override fun getAdditionalSessionProviders(context: Context): List<SessionProvider>? {
        return null
    }

    companion object {
        const val RECEIVER_ID = "24471AF3"
    }
}