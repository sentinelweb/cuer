package uk.co.sentinelweb.cuer.app.ui.cast

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import uk.co.sentinelweb.cuer.shared.generated.resources.*

object CastDialogComposeables {

    @Composable
    fun CastDialogUi(viewModel: CastDialogViewModel) {
        val state = viewModel.model.collectAsState(initial = CastDialogModel.blank)
        CastDialogView(state.value, viewModel)
    }

    @Composable
    fun CastDialogView(model: CastDialogModel, viewModel: CastDialogViewModel) {
        Column {
            Header(model)
            CuerCastRow(model.cuerCastStatus, viewModel)
            ChromeCastRow(model.chromeCastStatus, viewModel)
        }
    }

    @Composable
    private fun Header(model: CastDialogModel) {
        Box(
            modifier = Modifier.height(80.dp)
                .fillMaxWidth()
                .background(Color.Blue)
        ) {
//            Image(
//                painter = painterResource(Res.drawable.cast_header_640_T9rKvI3N0NM_unsplash),
//                contentDescription = "Header image",
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(160.dp)
//                    .wrapContentHeight(),
//                contentScale = ContentScale.Crop
//            )
            Text(
                text = stringResource(Res.string.cast_dialog_title),
                color = Color.White,
                fontSize = 20.sp,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp),
            )
            Text(
                text = model.connectionStatus,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp),
            )
        }
    }

    @Composable
    private fun CuerCastRow(model: CastDialogModel.CuerCastStatus, viewModel: CastDialogViewModel) {
        Box(
            modifier = Modifier.background(Color.White)
                .fillMaxWidth()
                .height(64.dp)
        ) {
            Row(Modifier
                .align(Alignment.CenterStart)
                .height(64.dp)
                .clickable { viewModel.connectCuerCast() }
            ) {
                val icon = if (model.isConnected) Res.drawable.ic_cuer_cast_connected else Res.drawable.ic_cuer_cast
                Image(
                    painter = painterResource(icon),
                    contentDescription = "Cuer cast Icon",
                    modifier = Modifier
                        .size(48.dp)
                        .padding(8.dp)
                        .align(Alignment.CenterVertically)

                )
                Column(
                    Modifier
                        .align(Alignment.CenterVertically)
                        .fillMaxWidth(if (model.isConnected) 0.8f else 1.0f)
                ) {
                    Text(
                        text = model.connectedHost ?: stringResource(Res.string.cast_dialog_not_connected),
                        color = Color.Black,
                        modifier = Modifier
                            .padding(16.dp),
                    )
                }

            }
            if (model.isConnected) {
                Row(
                    Modifier
                        .align(Alignment.CenterEnd)
                        .height(64.dp)
                ) {
                    if (model.isPlaying) {
                        Image(
                            painter = painterResource(Res.drawable.ic_stop),
                            contentDescription = "Cuer cast Icon",
                            modifier = Modifier
                                .size(48.dp)
                                .padding(8.dp)
                                .align(Alignment.CenterVertically)
                                .clickable { viewModel.stopCuerCast() }
                        )
                    }
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = "Cuer cast disconnect",
                        modifier = Modifier
                            .height(48.dp)
                            .padding(8.dp)
                            .align(Alignment.CenterVertically)
                            .clickable { viewModel.disconnectCuerCast() }
                    )

                }
            }
        }
    }

    @Composable
    private fun ChromeCastRow(model: CastDialogModel.ChromeCastStatus, viewModel: CastDialogViewModel) {
        Box(
            modifier = Modifier.background(Color.White)
                .fillMaxWidth()
                .height(64.dp)
        ) {
            Row(Modifier
                .align(Alignment.CenterStart)
                .height(64.dp)
                .clickable { viewModel.connectChromeCast() }
            ) {
                val icon = if (model.isConnected) Res.drawable.ic_chromecast_connected else Res.drawable.ic_chromecast
                Image(
                    painter = painterResource(icon),
                    contentDescription = "Cuer cast Icon",
                    modifier = Modifier
                        .size(48.dp)
                        .padding(8.dp)
                        .align(Alignment.CenterVertically)
                )
                Column(
                    Modifier
                        .align(Alignment.CenterVertically)
                        .fillMaxWidth(if (model.isConnected) 0.8f else 1.0f)
                ) {
                    Text(
                        text = model.connectedHost ?: stringResource(Res.string.cast_dialog_not_connected),
                        color = Color.Black,
                        modifier = Modifier
                            .padding(16.dp),
                    )
                }
            }
            if (model.isConnected) {
                Icon(
                    Icons.Default.Clear,
                    contentDescription = "Cuer cast disconnect",
                    modifier = Modifier
                        .height(48.dp)
                        .padding(8.dp)
                        .align(Alignment.CenterEnd)
                        .clickable { viewModel.disconnectChromeCast() }
                )
            }
        }
    }
}