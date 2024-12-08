package uk.co.sentinelweb.cuer.hub.util.system_tray

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import uk.co.sentinelweb.cuer.app.ui.common.compose.CuerSharedTheme
import uk.co.sentinelweb.cuer.hub.ui.home.HomeUiCoordinator

object ComposeSystemTrayPopup : KoinComponent {

    private val homeUiCoordinator by inject<HomeUiCoordinator>()

    @Composable
    fun SystemTrayComposePopup() {
        CuerSharedTheme {
            val homeState = homeUiCoordinator.modelObservable.collectAsState()
            if (homeState.value.showPlayer) {
                Box(modifier = Modifier.padding(8.dp, bottom = 16.dp)) {
                    homeUiCoordinator.playerUiCoordinator
                        ?.PlayerSystrayUi()
                        ?: Text("Not playing ...", color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}
