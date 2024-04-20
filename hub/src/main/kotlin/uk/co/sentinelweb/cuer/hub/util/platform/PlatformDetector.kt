package uk.co.sentinelweb.cuer.hub.util.platform

import uk.co.sentinelweb.cuer.domain.NodeDomain

fun getNodeDeviceType(): NodeDomain.DeviceType {
    val osName = System.getProperty("os.name").toLowerCase()
    return when {
        osName.contains("linux") -> NodeDomain.DeviceType.LINUX
        osName.contains("windows") -> NodeDomain.DeviceType.WINDOWS
        osName.contains("mac") -> NodeDomain.DeviceType.MAC
        osName.contains("android") -> NodeDomain.DeviceType.ANDROID
        else -> NodeDomain.DeviceType.OTHER
    }
}

fun getOSData(): String {
    val osName = System.getProperty("os.name")
    val osArch = System.getProperty("os.arch")
    val osVersion = System.getProperty("os.version")
    return "$osName - $osVersion"
}