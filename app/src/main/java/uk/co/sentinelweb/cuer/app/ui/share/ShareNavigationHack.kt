package uk.co.sentinelweb.cuer.app.ui.share

/**
 * Used to stop onUserLeaveHint finishing because of
 * https://github.com/sentinelweb/cuer/issues/279
 *
 * not used in main but injected
 */
class ShareNavigationHack {
    var isNavigatingInApp = false
}