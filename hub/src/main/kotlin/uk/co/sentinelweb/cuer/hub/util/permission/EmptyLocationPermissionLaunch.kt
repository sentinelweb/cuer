package uk.co.sentinelweb.cuer.hub.util.permission

import uk.co.sentinelweb.cuer.app.util.permission.LocationPermissionLaunch

class EmptyLocationPermissionLaunch : LocationPermissionLaunch {
    override fun launchLocationPermission() = Unit
}