package uk.co.sentinelweb.cuer.app.util.cast

import android.content.Context

import com.google.android.gms.cast.framework.CastOptions
import com.google.android.gms.cast.framework.OptionsProvider
import com.google.android.gms.cast.framework.SessionProvider

class CastOptionsProvider : OptionsProvider {
    override fun getCastOptions(appContext: Context): CastOptions {

        // Register your receiver on Cast Developer Console to get this ID: https://cast.google.com/publish
        val receiverId = "C5CBE8CA" // todo change this

        return CastOptions.Builder()
            .setReceiverApplicationId(receiverId)
            .build()
    }

    override fun getAdditionalSessionProviders(context: Context): List<SessionProvider>? {
        return null
    }
}