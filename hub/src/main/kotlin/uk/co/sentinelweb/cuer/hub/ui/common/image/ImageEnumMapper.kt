package uk.co.sentinelweb.cuer.hub.ui.common.image

import uk.co.sentinelweb.cuer.domain.NodeDomain

object ImageEnumMapper {
    fun map(type: NodeDomain.DeviceType) = when (type) {
        NodeDomain.DeviceType.ANDROID -> "drawable/ic_android.svg"
        NodeDomain.DeviceType.IOS -> "drawable/ic_iphone.svg"
        NodeDomain.DeviceType.WEB -> "drawable/ic_browse.svg"
        NodeDomain.DeviceType.MAC -> "drawable/ic_mac.svg"
        NodeDomain.DeviceType.WINDOWS -> "drawable/ic_windows.svg"
        NodeDomain.DeviceType.LINUX -> "drawable/ic_linux.svg"
        NodeDomain.DeviceType.OTHER -> "drawable/ic_"
    }
}