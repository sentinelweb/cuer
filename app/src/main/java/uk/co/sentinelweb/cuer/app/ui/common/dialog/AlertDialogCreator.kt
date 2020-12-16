package uk.co.sentinelweb.cuer.app.ui.common.dialog

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import uk.co.sentinelweb.cuer.app.R

class AlertDialogCreator(
    private val context: Context
) {

    fun create(model: AlertDialogModel): AlertDialog =
        MaterialAlertDialogBuilder(context)
            .setTitle(model.title)
            .setMessage(model.message)
            .setPositiveButton(R.string.ok, object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, p1: Int) {
                    model.confirmAction()
                    dialog?.dismiss()
                }
            })
            .setNegativeButton(R.string.cancel, object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, p1: Int) {
                    dialog?.dismiss()
                }
            })
            .create()
}