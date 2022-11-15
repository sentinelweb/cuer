package uk.co.sentinelweb.cuer.core.wrapper

interface ConnectivityWrapper {
    fun isConnected(): Boolean
    fun isMetered(): Boolean
}