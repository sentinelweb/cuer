package uk.co.sentinelweb.cuer.hub.util.system_tray

import androidx.compose.foundation.layout.Row
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import org.koin.core.component.KoinComponent
import uk.co.sentinelweb.cuer.app.ui.common.compose.CuerSharedTheme
import java.awt.SystemTray

object SystemTrayPopup: KoinComponent {
    @Composable
    fun CustomPopupContent(onClose: () -> Unit) {
        CuerSharedTheme {
            Row {
                Button(onClick = { println("Play clicked") }) {
                    Text("▶")
                }
                Button(onClick = { println("Pause clicked") }) {
                    Text("️⏸")
                }
                Button(onClick = { println("Stop clicked") }) {
                    Text("🛑")
                }
                Button(onClick = { onClose() }) {
                    Text("❌")
                }
            }
        }
    }
}
