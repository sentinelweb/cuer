package uk.co.sentinelweb.cuer.app.ui.common.dialog

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import uk.co.sentinelweb.cuer.app.R

class AlertDialogCreator(
    private val context: Context
) {

    fun create(model: AlertDialogModel): AlertDialog {

        val builder = MaterialAlertDialogBuilder(context)
            .setTitle(model.title)
            .setMessage(model.message)
            .setPositiveButton(model.confirm.label, object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, p1: Int) {
                    model.confirm.action()
                    dialog?.dismiss()
                }
            })
        model.neutral?.apply {
            builder
                .setNeutralButton(label, object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, p1: Int) {
                        action()
                        dialog?.dismiss()
                    }
                })
        }
        builder
            .setNegativeButton(model.cancel?.label ?: R.string.cancel, object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, p1: Int) {
                    model.cancel?.let { it.action() }
                    dialog?.dismiss()
                }
            })
        return builder.create()
    }
}