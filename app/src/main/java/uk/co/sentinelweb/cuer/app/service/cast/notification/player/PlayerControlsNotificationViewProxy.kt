package uk.co.sentinelweb.cuer.app.service.cast.notification.player

import android.app.Service
import org.koin.android.scope.AndroidScopeComponent
import org.koin.core.component.KoinScopeComponent
import org.koin.core.scope.Scope
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferences.Companion.PlayerNotificationType.Custom
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferences.Companion.PlayerNotificationType.Media
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferencesWrapper

class PlayerControlsNotificationViewProxy(
    private val service: Service,
    private val preferences: MultiPlatformPreferencesWrapper,
    serviceScopeComponent: AndroidScopeComponent
) : PlayerControlsNotificationContract.View, KoinScopeComponent {

    override val scope: Scope = serviceScopeComponent.scope
    private var target: PlayerControlsNotificationContract.View? = null
    private var icon: Int = -1

    private fun getOrCreateTarget(): PlayerControlsNotificationContract.View {
        if (target == null) {
            target = if (preferences.playerNotificationType == Media) {
                scope.get<PlayerControlsNotificationMedia>()
            } else {
                scope.get<PlayerControlsNotificationCustom>()
            }
            if (icon != -1) {
                target?.setIcon(icon)
            }
        }
        return target ?: throw NullPointerException("Target cannot be null")
    }

    override fun showNotification(state: PlayerControlsNotificationContract.State) {
        getOrCreateTarget().showNotification(state)
    }

    override fun stopSelf() {
        service.stopSelf()
    }

    override fun setIcon(icon: Int) {
        this.icon = icon
        getOrCreateTarget().setIcon(icon)
    }

    override fun onDeleteAction() {
        preferences.playerNotificationType = if (preferences.playerNotificationType == Media) Custom else Media
        target = null
    }
}
