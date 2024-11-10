package uk.co.sentinelweb.cuer.core.providers

import uk.co.sentinelweb.cuer.domain.NodeDomain
import uk.co.sentinelweb.cuer.domain.NodeDomain.DeviceType.*
import java.util.*

actual fun getOS(): NodeDomain.DeviceType {
    val osName = System.getProperty("os.name").lowercase(Locale.getDefault())
    return when {
        osName.contains("linux") -> LINUX
        osName.contains("windows") -> WINDOWS
        osName.contains("mac") -> MAC
        osName.contains("android") -> ANDROID
        else -> OTHER
    }
}