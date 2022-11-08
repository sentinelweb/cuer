package uk.co.sentinelweb.cuer.app.util.wrapper

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import uk.co.sentinelweb.cuer.core.wrapper.ConnectivityWrapper

class AndroidConnectivityWrapper constructor(
    private val context: Context
) : ConnectivityWrapper {

    private val cm: ConnectivityManager by lazy { context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager }
    override fun isConnected(): Boolean {
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        return activeNetwork?.isConnectedOrConnecting == true
    }

    override fun isMetered(): Boolean = cm.isActiveNetworkMetered()
}