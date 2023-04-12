package uk.co.sentinelweb.cuer.app.service.update

import android.app.Service
import android.content.Intent
import android.os.IBinder
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.core.scope.Scope
import uk.co.sentinelweb.cuer.app.util.extension.serviceScopeWithSource
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.MediaDomain


class UpdateService() : Service(), UpdateServiceContract.Service, AndroidScopeComponent {

    override val scope: Scope by serviceScopeWithSource()
    private val controller: UpdateServiceContract.Controller by scope.inject()
    private val log: LogWrapper by inject()

    override fun onCreate() {
        super.onCreate()
        log.tag(this)
        _instance = this
        log.d("Update Service created")
        controller.initialise()
    }

    override fun onDestroy() {
        super.onDestroy()
        log.d("Update Service destroyed")
        controller.destroy()
        scope.close()
        _instance = null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // this routes media buttons to the MediaSessionCompat
        controller.handleAction(intent?.action)
        return START_NOT_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? = null

    override fun notify(mediaDomains: List<MediaDomain>) {
        stopSelf()
    }

    companion object {
        private var _instance: UpdateService? = null
        fun instance(): UpdateService? = _instance

    }
}
