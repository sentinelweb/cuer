package uk.co.sentinelweb.cuer.app.ui.common.mapper

import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.domain.NodeDomain

object ImageEnumMapper {
    fun map(type: NodeDomain.DeviceType) = when (type) {
        NodeDomain.DeviceType.ANDROID -> R.drawable.ic_android
        NodeDomain.DeviceType.IOS -> R.drawable.ic_iphone
        NodeDomain.DeviceType.WEB -> R.drawable.ic_browse
        NodeDomain.DeviceType.MAC -> R.drawable.ic_mac
        NodeDomain.DeviceType.WINDOWS -> R.drawable.ic_windows
        NodeDomain.DeviceType.LINUX -> R.drawable.ic_linux
        NodeDomain.DeviceType.OTHER -> R.drawable.ic_clear
    }
}