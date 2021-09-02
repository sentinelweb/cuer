package uk.co.sentinelweb.cuer.app.ui.common.navigation

class EmptyNavigationProvider : NavigationProvider {
    override fun navigate(destination: NavigationModel) = Unit

    override fun navigate(id: Int) = Unit

    override fun checkForPendingNavigation(target: NavigationModel.Target?) = null

    override fun clearPendingNavigation(target: NavigationModel.Target) = Unit
}