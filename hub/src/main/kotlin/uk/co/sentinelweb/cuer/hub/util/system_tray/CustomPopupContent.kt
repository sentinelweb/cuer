package uk.co.sentinelweb.cuer.hub.util.system_tray

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import uk.co.sentinelweb.cuer.app.ui.common.compose.CuerSharedTheme
import uk.co.sentinelweb.cuer.app.ui.player.PlayerComposeables
import uk.co.sentinelweb.cuer.hub.ui.home.HomeUiCoordinator
import java.awt.SystemTray

object SystemTrayPopup: KoinComponent {
    private val homeUiCoordinator by inject<HomeUiCoordinator>()
    @Composable
    fun CustomPopupContent() {
        CuerSharedTheme {
            val state = homeUiCoordinator.modelObservable.collectAsState()
            if (state.value.showPlayer) {
                Box(modifier = Modifier.padding(8.dp, bottom=16.dp)) {
                    homeUiCoordinator.playerUiCoordinator
                        ?.PlayerDesktopUi()
                        ?:Text("Not playing ...")
                }
            }
        }
    }
}
