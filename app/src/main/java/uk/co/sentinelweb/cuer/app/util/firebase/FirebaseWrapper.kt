package uk.co.sentinelweb.cuer.app.util.firebase

import android.content.Context
import com.google.firebase.FirebaseApp

class FirebaseWrapper constructor(
    private val appContext: Context,
    private val imageProvider: FirebaseDefaultImageProvider
) {

    fun init() {
        FirebaseApp.initializeApp(appContext)
        imageProvider.checkToInit()
    }
}