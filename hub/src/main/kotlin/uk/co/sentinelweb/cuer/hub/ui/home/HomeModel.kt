package uk.co.sentinelweb.cuer.hub.ui.home

// todo add fields - holds main UI state
data class HomeModel(
    val route: DisplayRoute = DisplayRoute.Settings
) {
    enum class DisplayRoute {
        Settings, Files
    }

    companion object {
        val blankModel: HomeModel = HomeModel(HomeModel.DisplayRoute.Settings)
    }
}