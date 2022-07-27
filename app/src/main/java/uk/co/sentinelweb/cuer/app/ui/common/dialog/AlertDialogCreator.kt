package uk.co.sentinelweb.cuer.app.ui.common.dialog

import android.content.Context
import android.content.DialogInterface
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.setPadding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import uk.co.sentinelweb.cuer.app.R

class AlertDialogCreator(
    private val context: Context
) {

    fun create(model: AlertDialogModel): AlertDialog {
        val builder = MaterialAlertDialogBuilder(context)
            .setTitle(model.title)
            .setPositiveButton(model.confirm.label, object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, p1: Int) {
                    model.confirm.action()
                    dialog?.dismiss()
                }
            })
        val msg = context.getString(model.message)
        if (msg.indexOf("\n")>-1) {
            TextView(context,null, 0, R.style.TextAppearance_App_Body1)
                .apply {
                    text = msg
                    setPadding(context.resources.getDimensionPixelSize(R.dimen.page_margin))
                }
                .apply { builder.setView(this) }
        } else {
            builder.setMessage(model.message)
        }
        model.neutral?.apply {
            builder
                .setNeutralButton(label, object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, p1: Int) {
                        action()
                        dialog?.dismiss()
                    }
                })
        }
        model.cancel?.apply {
            builder
                .setNegativeButton(model.cancel.label, object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, p1: Int) {
                        model.cancel.let { it.action() }
                        dialog?.dismiss()
                    }
                })
        }
        model.dismiss?.also {
            builder.setOnDismissListener { it() }
        }
        return builder.create()
    }
}