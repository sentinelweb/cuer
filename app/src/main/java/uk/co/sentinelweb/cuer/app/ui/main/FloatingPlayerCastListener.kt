package uk.co.sentinelweb.cuer.app.ui.main

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManagerListener
import uk.co.sentinelweb.cuer.app.ui.ytplayer.floating.FloatingPlayerServiceManager
import uk.co.sentinelweb.cuer.app.util.cast.ChromeCastWrapper

class FloatingPlayerCastListener constructor(
    private val activity: MainActivity,
    private val wrapper: ChromeCastWrapper,
    private val floatingPlayerServiceManager: FloatingPlayerServiceManager,

    ) : SessionManagerListener<CastSession> {

    fun observeConnection() {
        activity.lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
            fun connectListener() {
                listen()
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun disconnectListener() {
                release()
            }

        })
    }

    fun listen() {
        wrapper.addSessionListener(this)
    }

    fun release() {
        wrapper.removeSessionListener(this)
    }

    override fun onSessionStarted(p0: CastSession, p1: String) {
        floatingPlayerServiceManager.stop()
    }

    override fun onSessionStarting(p0: CastSession) = Unit

    override fun onSessionStartFailed(p0: CastSession, p1: Int) = Unit

    override fun onSessionEnding(p0: CastSession) = Unit

    override fun onSessionEnded(p0: CastSession, p1: Int) = Unit

    override fun onSessionResuming(p0: CastSession, p1: String) = Unit

    override fun onSessionResumed(p0: CastSession, p1: Boolean) = Unit

    override fun onSessionResumeFailed(p0: CastSession, p1: Int) = Unit

    override fun onSessionSuspended(p0: CastSession, p1: Int) = Unit

}