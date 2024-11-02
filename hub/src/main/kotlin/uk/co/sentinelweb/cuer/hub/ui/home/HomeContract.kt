package uk.co.sentinelweb.cuer.hub.ui.home

import uk.co.sentinelweb.cuer.domain.NodeDomain

class HomeContract {

    data class HomeModel(
        val route: DisplayRoute = DisplayRoute.Settings
    ) {

        sealed class DisplayRoute {
            object Settings : DisplayRoute()
            data class Folders(val node: NodeDomain? = null) : DisplayRoute()
            object ThemeTest : DisplayRoute()
            object LocalConfig : DisplayRoute()
        }

        companion object {
            val Initial: HomeModel = HomeModel(DisplayRoute.Folders())
        }
    }

    sealed class Label {
        object None : Label()
        data class ErrorMessage(
            val message: String,
            val unique: Double = Math.random()
        ) : Label()
    }
}
