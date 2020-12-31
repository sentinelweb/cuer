package uk.co.sentinelweb.cuer.app.util.firebase

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase

class FirebaseWrapper constructor(
    private val appContext: Context,
    private val imageProvider: FirebaseDefaultImageProvider
) {

    fun init() {
        FirebaseApp.initializeApp(appContext)
        imageProvider.checkToInit()
        Firebase.crashlytics.setCrashlyticsCollectionEnabled(true)

    }

    fun logException(t: Throwable) {
        Firebase.crashlytics.recordException(t)
    }

    fun sendUnsentReports() {
        Firebase.crashlytics.sendUnsentReports()
    }

    fun hasUnsentReports() =
        Firebase.crashlytics.checkForUnsentReports().result ?: false// todo check no db/net on main thread

    fun addCrashlyticKey(k: String, v: String) {
        Firebase.crashlytics.setCustomKey(k, v)
    }

    fun setCrashlyticTag(v: String) {
        Firebase.crashlytics.setCustomKey(KEY_TAG, v)
    }

    fun logMessage(msg: String) {
        Firebase.crashlytics.log(msg)
    }

    companion object {
        private const val KEY_TAG = "tag"
    }
}