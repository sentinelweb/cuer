package uk.co.sentinelweb.cuer.hub.util.platform

fun getOSData(): String {
    val osName = System.getProperty("os.name")
    val osArch = System.getProperty("os.arch")
    val osVersion = System.getProperty("os.version")
    return "$osName - $osVersion"
}