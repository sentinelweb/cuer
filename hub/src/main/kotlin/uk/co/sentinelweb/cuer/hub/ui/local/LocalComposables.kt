package uk.co.sentinelweb.cuer.hub.ui.local

import androidx.compose.foundation.layout.Column
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import uk.co.sentinelweb.cuer.hub.ui.common.image.ImageSvg

object LocalComposables {
    @Composable
    fun showDialog(
        isDialogOpen: Boolean,
        onClose: () -> Unit,
    ) {
        var textFieldValue by remember { mutableStateOf("") }

        if (isDialogOpen) {
            AlertDialog(
                onDismissRequest = onClose,
                title = { Text(text = "Dialog Title") },
                buttons = {
                    Button(onClick = onClose) {
                        ImageSvg("drawable/ic_clear.svg")
                        Text("Close")
                    }
                },
                text = {
                    LocalUi(textFieldValue)
                }
            )
        }
    }

    @Composable
    fun LocalUi(textFieldValue: String) {
        var textFieldValue1 = textFieldValue
        Column {
            Text("Dialog Body")
            TextField(value = textFieldValue1, onValueChange = { textFieldValue1 = it })
        }
    }
}