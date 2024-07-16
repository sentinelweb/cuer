package uk.co.sentinelweb.cuer.app.util.chromecast

import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManagerListener
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

// https://developers.google.com/android/reference/com/google/android/gms/cast/framework/CastSession
class CuerCastSessionListener constructor(
    private val chromeCastWrapper: ChromeCastWrapper,
    private val log: LogWrapper
) : SessionManagerListener<CastSession> {

    private var _currentCastSession: CastSession? = null
    val currentCastSession: CastSession?
        get() = _currentCastSession

    init {
        log.tag(this)
    }

    fun listen() {
        chromeCastWrapper.addSessionListener(this)
    }

    fun destroy() {
        chromeCastWrapper.removeSessionListener(this)
    }

    override fun onSessionStarted(castSession: CastSession, p1: String) {
        //log.d("onSessionStarted $castSession")
        _currentCastSession = castSession
    }

    override fun onSessionStarting(castSession: CastSession) {
        //log.d("onSessionStarting $castSession")
    }

    override fun onSessionStartFailed(castSession: CastSession, p1: Int) {
        //log.d("onSessionStartFailed $castSession")
        _currentCastSession = null
    }

    override fun onSessionEnding(castSession: CastSession) {
        //log.d("onSessionEnding $castSession")
    }

    override fun onSessionEnded(castSession: CastSession, p1: Int) {
        //log.d("onSessionEnded $castSession")
        _currentCastSession = null
    }

    override fun onSessionResuming(castSession: CastSession, p1: String) {
        //log.d("onSessionResuming $castSession")
    }

    override fun onSessionResumed(castSession: CastSession, p1: Boolean) {
        //log.d("onSessionResumed $castSession")
        _currentCastSession = castSession
    }

    override fun onSessionResumeFailed(castSession: CastSession, p1: Int) {
        //log.d("onSessionResumeFailed $castSession")
        _currentCastSession = null
    }

    override fun onSessionSuspended(castSession: CastSession, p1: Int) {
        //log.d("onSessionSuspended $castSession")
        _currentCastSession = null
    }
}
