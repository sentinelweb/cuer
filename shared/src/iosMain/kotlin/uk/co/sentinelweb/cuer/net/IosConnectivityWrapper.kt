package uk.co.sentinelweb.cuer.net

import uk.co.sentinelweb.cuer.core.wrapper.ConnectivityWrapper

class IosConnectivityWrapper : ConnectivityWrapper {
    override fun isConnected(): Boolean = true

    override fun isMetered(): Boolean = false
}