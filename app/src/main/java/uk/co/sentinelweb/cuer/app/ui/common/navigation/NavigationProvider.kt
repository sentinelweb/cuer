package uk.co.sentinelweb.cuer.app.ui.common.navigation

interface NavigationProvider {

    fun navigate(destination: NavigationModel)

    fun checkForPendingNavigation(target: NavigationModel.Target?): NavigationModel?

    fun clearPendingNavigation(target: NavigationModel.Target)
}