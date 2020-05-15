package uk.co.sentinelweb.cuer.app.ui.common.dialog

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder

// possibly should be an MV* but is quite small
class SelectDialogCreator(
    private val context: Context
) {

    fun create(
        model: SelectDialogModel,
        listener: DialogInterface.OnMultiChoiceClickListener
    ): AlertDialog {
        return MaterialAlertDialogBuilder(context)
            .setTitle(model.title)
            .setMultiChoiceItems(
                model.items.map { it.name }.toTypedArray(),
                model.items.map { it.selected }.toBooleanArray(),
                listener
            )
            .create()
    }
}