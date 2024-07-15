package uk.co.sentinelweb.cuer.app.ui.filebrowser

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import uk.co.sentinelweb.cuer.app.ui.common.compose.CuerTheme
import uk.co.sentinelweb.cuer.app.ui.common.compose.topappbar.Action
import uk.co.sentinelweb.cuer.app.ui.common.compose.topappbar.CuerMenuItem
import uk.co.sentinelweb.cuer.app.ui.common.compose.topappbar.CuerTopAppBarComposables

object FileBrowserAppComposeables {

    // todo use scaffold
    @Composable
    fun FileBrowserAppWrapperUi(interactions: FilesContract.Interactions) {
        CuerTheme {
            Surface {
                Column {
                    Box(contentAlignment = Alignment.TopStart) {
                        CuerTopAppBarComposables.CuerAppBar(
                            text = "Files",
                            backgroundColor = MaterialTheme.colors.primary,
                            onUp = { },
                            actions = listOf(
                                Action(CuerMenuItem.Help, { }),
                                Action(CuerMenuItem.Settings, { }),
                            ),
                            modifier = Modifier
                                .height(56.dp)
                        )

                    }
                    FilesComposeables.FilesUi(interactions = interactions)
                }
            }
        }
    }
}
