package uk.co.sentinelweb.cuer.hub.ui.remotes.selector

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.AlertDialog
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import org.jetbrains.skia.Surface
import uk.co.sentinelweb.cuer.app.ui.remotes.selector.RemotesDialogComposeables
import uk.co.sentinelweb.cuer.app.ui.remotes.selector.RemotesDialogContract
import uk.co.sentinelweb.cuer.hub.ui.remotes.selector.RemotesDialogLauncher.DisplayModel

object RemotesDialogLauncherComposeables {

    @Composable
    fun ShowRemotesDialogIfNecessary(remotesDialogLauncher: RemotesDialogLauncher) {
        val displayModel = remotesDialogLauncher.modelObservable.collectAsState(DisplayModel.blankModel)

        if (displayModel.value.isSelectRemotesVisible) {
            Dialog(onDismissRequest = { remotesDialogLauncher.hideRemotesDialog() }) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    elevation = 24.dp
                ) {
                    Box(
                        modifier = Modifier
                            .wrapContentSize()
                            .background(Color.White)
                    ) {
                        RemotesDialogComposeables.RemotesDialogUi(remotesDialogLauncher.viewModel)
                    }
                }
            }
        }
    }
}
