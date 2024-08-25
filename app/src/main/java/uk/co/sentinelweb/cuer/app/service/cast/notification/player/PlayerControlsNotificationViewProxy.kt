package uk.co.sentinelweb.cuer.app.service.cast.notification.player

import android.app.Service
import org.koin.android.scope.AndroidScopeComponent
import org.koin.core.component.KoinScopeComponent
import org.koin.core.scope.Scope

class PlayerControlsNotificationViewProxy(
    private val service: Service,
//    private val preferences: MultiPlatformPreferencesWrapper,
    serviceScopeComponent: AndroidScopeComponent
) : PlayerControlsNotificationContract.View, KoinScopeComponent {

    override val scope: Scope = serviceScopeComponent.scope
    private var targets: MutableList<PlayerControlsNotificationContract.View> = mutableListOf()
    //private var icon: Int = -1

    init {
        targets.add(scope.get<PlayerControlsNotificationMedia>())
        targets.add(scope.get<PlayerControlsNotificationCustom>())
    }
//    private fun getOrCreateTarget(): PlayerControlsNotificationContract.View {
//        if (target == null) {
//            target = if (preferences.playerNotificationType == Media) {
//                scope.get<PlayerControlsNotificationMedia>()
//            } else {
//                scope.get<PlayerControlsNotificationCustom>()
//            }
//            if (icon != -1) {
//                target?.setIcon(icon)
//            }
//        }
//        return target ?: throw NullPointerException("Target cannot be null")
//    }

    override fun showNotification(state: PlayerControlsNotificationContract.State) {
        //getOrCreateTarget().showNotification(state)
        targets.forEach { it.showNotification(state) }
    }

    override fun stopSelf() {
        service.stopSelf()
    }

    override fun setIcon(icon: Int) {
        //this.icon = icon
        //getOrCreateTarget().setIcon(icon)
        targets.forEach { it.setIcon(icon) }
    }

    override fun onDeleteAction() {
        //preferences.playerNotificationType = if (preferences.playerNotificationType == Media) Custom else Media
        //target = null
        targets.forEach { it.onDeleteAction() }
    }
}
