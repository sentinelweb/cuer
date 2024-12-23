package uk.co.sentinelweb.cuer.hub.ui.preferences

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import uk.co.sentinelweb.cuer.app.ui.common.compose.CuerSharedTheme
import uk.co.sentinelweb.cuer.hub.ui.common.button.HeaderButton
import javax.swing.JFileChooser

object PreferenceComposeables {
    @Composable
    fun PreferencesUi(coordinator: PreferencesUiCoordinator) {
        val state = coordinator.modelObservable.collectAsState(initial = PreferencesModel.blankModel())
        PreferencesView(state.value, coordinator)
    }

    @Composable
    private fun PreferencesView(model: PreferencesModel, view: PreferencesUiCoordinator) {
        CuerSharedTheme {
            Column(modifier = Modifier.padding(8.dp)) {
                FolderPaths(model, view)
                DbInitCheckBoxRow(model, view)
            }
        }
    }

    @Composable
    fun FolderPaths(model: PreferencesModel, view: PreferencesUiCoordinator) {
        Column {
            HeaderButton("Add folder", "drawable/ic_add.svg") {
                val chooser = JFileChooser()
                chooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
                chooser.dialogTitle = "Add media folder"
                val result = chooser.showOpenDialog(null)
                if (result == JFileChooser.APPROVE_OPTION) {
                    view.addFolder(chooser.selectedFile.path)
                }
            }

            model.folderRoots.forEach { path ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = path, color = Color.White)

                    HeaderButton(
                        "Remove",
                        "drawable/ic_delete.svg",
                        modifier = Modifier.padding(start = 8.dp)
                    ) { view.removeFolder(path) }
                }
            }
        }
    }

    @Composable
    fun DbInitCheckBoxRow(model: PreferencesModel, view: PreferencesUiCoordinator) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxSize()) {
            Checkbox(
                checked = model.isDatabaseInitialised,
                enabled = false,
                onCheckedChange = { view.setDataBaseInitialised(it) }
            )

            Text(
                text = "Database initialised",
                color = Color.White,
                modifier = Modifier.padding(start = 8.dp)
            )

        }
    }
}
