package uk.co.sentinelweb.cuer.app.ui.remotes.selector

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import uk.co.sentinelweb.cuer.app.ui.remotes.RemotesContract
import uk.co.sentinelweb.cuer.shared.generated.resources.Res
import uk.co.sentinelweb.cuer.shared.generated.resources.ic_tv
import uk.co.sentinelweb.cuer.shared.generated.resources.remotes_dialog_title

object RemotesDialogComposeables {

    @Composable
    fun RemotesDialogUi(viewModel: RemotesDialogViewModel) {
        val state = viewModel.model.collectAsState(initial = RemotesDialogContract.Model.blank)
        RemotesDialogView(state.value, viewModel)
    }

    @Composable
    fun RemotesDialogView(model: RemotesDialogContract.Model, viewModel: RemotesDialogViewModel) {
        Column {
            Header(model)
            model.remotes.forEach {
                RemoteRow(it, viewModel)
            }
        }
    }

    @Composable
    private fun Header(model: RemotesDialogContract.Model) {
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
                text = stringResource(Res.string.remotes_dialog_title),
                color = Color.White,
                fontSize = 20.sp,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp),
            )
        }
    }

    @Composable
    private fun RemoteRow(
        remote: RemotesContract.View.RemoteNodeModel,
        viewModel: RemotesDialogViewModel
    ) {
        // fixme why isAvailable false?
        val contentColor = remote.domain.isAvailable
            .takeIf { it }
            ?.let { MaterialTheme.colors.onSurface }
            ?: MaterialTheme.colors.onSurface.copy(alpha = 0.3f)
//        val contentColor = MaterialTheme.colors.onSurface
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .background(MaterialTheme.colors.surface)
                    .clickable { viewModel.onNodeSelected(remote.domain) },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(RemotesIconMapper.map(remote.deviceType)),
                    contentDescription = "Remote Icon",
                    modifier = Modifier
                        .size(48.dp)
                        .padding(8.dp)
                        .align(Alignment.CenterVertically)

                )
                Column { // todo use textview
                    Text(
                        text = remote.title,
                        color = contentColor,
                        style = MaterialTheme.typography.h4,
                        modifier = Modifier
                            .padding(start = 8.dp),
                    )
                    Text(
                        text = remote.address,
                        color = contentColor,
                        style = MaterialTheme.typography.body2,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                    Text(
                        text = "${remote.deviceType} : ${remote.device} : ${remote.authType}",
                        color = contentColor,
                        style = MaterialTheme.typography.body2,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
            remote.screens.forEach { screen ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .background(MaterialTheme.colors.surface)
                        .clickable { viewModel.onScreenSelected(remote.domain, screen.domain) },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        painter = painterResource(Res.drawable.ic_tv),
                        contentDescription = "Remote Icon",
                        modifier = Modifier
                            .size(48.dp)
                            .padding(8.dp)
                            .align(Alignment.CenterVertically)
                    )
                    Column { // todo use textview
                        Text(
                            text = "${screen.index}. ${screen.name}",
                            color = contentColor,
                            style = MaterialTheme.typography.h6,
                            modifier = Modifier
                                .padding(start = 8.dp),
                        )
                        Text(
                            text = "${screen.width} x ${screen.height} @ ${screen.refreshRate} ",
                            color = contentColor,
                            style = MaterialTheme.typography.body2,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }
    }
}
