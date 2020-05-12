package uk.co.sentinelweb.cuer.app.ui.common

data class NavigationModel constructor(
    val target: Navigate,
    val params: Map<NavigateParam, Any> = mapOf()
) {
    enum class Navigate { LOCAL_PLAYER }
    enum class NavigateParam { MEDIA_ID }
}