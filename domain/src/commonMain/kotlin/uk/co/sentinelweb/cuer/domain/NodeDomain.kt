package uk.co.sentinelweb.cuer.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Identifier

@Serializable
open class NodeDomain : Domain {

    // todo make isDesktop function (mac, win, linux)
    // todo make isMobile function (android, ios)
    enum class DeviceType {
        ANDROID,
        IOS,
        WEB,
        MAC,
        WINDOWS,
        LINUX,
        OTHER
    }
}
