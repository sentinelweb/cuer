package uk.co.sentinelweb.cuer.app.ui.remotes.selector

import uk.co.sentinelweb.cuer.domain.NodeDomain
import uk.co.sentinelweb.cuer.shared.generated.resources.*

object NodesIconMapper {
    fun map(type: NodeDomain.DeviceType) = when (type) {
        NodeDomain.DeviceType.ANDROID -> Res.drawable.ic_android
        NodeDomain.DeviceType.IOS -> Res.drawable.ic_iphone
        NodeDomain.DeviceType.WEB -> Res.drawable.ic_browse
        NodeDomain.DeviceType.MAC -> Res.drawable.ic_mac
        NodeDomain.DeviceType.WINDOWS -> Res.drawable.ic_windows
        NodeDomain.DeviceType.LINUX -> Res.drawable.ic_linux
        NodeDomain.DeviceType.OTHER -> Res.drawable.ic_clear
    }
}
