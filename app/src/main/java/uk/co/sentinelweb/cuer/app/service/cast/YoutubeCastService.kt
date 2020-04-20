package uk.co.sentinelweb.cuer.app.service.cast

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.roche.mdas.util.wrapper.ToastWrapper
import org.koin.android.ext.android.inject
import org.koin.core.KoinComponent
import org.koin.ext.getOrCreateScope
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.util.wrapper.NotificationWrapper

class YoutubeCastService : Service(), KoinComponent {

    private lateinit var controller : YoutubeCastServiceController
    private val toastWrapper: ToastWrapper by inject()
    private val notif: NotificationWrapper by inject()
    private lateinit var notifChannelId:String // todo move to appstate

    override fun onCreate() {
        super.onCreate()
        _instance = this
        val s = this.getOrCreateScope()
        controller = s.get()
        controller.initialise()
        toastWrapper.showToast("Service created")
        notifChannelId = notif.createChannelId()// todo create earlier?
        startForeground(FOREGROUND_ID,notification().build())
    }

    // todo move to wrapper
    fun notification() = NotificationCompat.Builder(this, notifChannelId)
        .setDefaults(Notification.DEFAULT_ALL)
        .setSmallIcon(R.drawable.ic_player_fast_rewind_black)
        .setContentTitle("service")
        .setContentText("content")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)
        .setWhen(System.currentTimeMillis())


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        toastWrapper.showToast("Service destroyed")
        controller.destroy()
        _instance = null
    }

    override fun onBind(p0: Intent?): IBinder? = null

    companion object {
        private const val FOREGROUND_ID = 34563
        private var _instance : YoutubeCastService? = null

        fun instance() : YoutubeCastService? = _instance
    }


}