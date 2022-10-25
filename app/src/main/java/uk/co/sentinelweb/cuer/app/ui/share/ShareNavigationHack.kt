package uk.co.sentinelweb.cuer.app.ui.share

/**
 * Used to stop onUserLeaveHint finishing because of
 * https://github.com/sentinelweb/cuer/issues/279
 */
class ShareNavigationHack {
    var isNavigatingInApp = false
}