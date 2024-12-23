package uk.co.sentinelweb.cuer.app.ui.common.dialog

import android.content.Context
import android.content.DialogInterface
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.setPadding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.common.resources.StringDecoder

class AlertDialogCreator(
    private val context: Context,
    private val strings: StringDecoder
) : AlertDialogContract.Creator {

    override fun create(model: AlertDialogModel): AlertDialog {
        val builder = MaterialAlertDialogBuilder(context)
            .setTitle(model.title)
            .setPositiveButton(strings.get(model.confirm.label), object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, p1: Int) {
                    model.confirm.action()
                    dialog?.dismiss()
                }
            })

        if (model.message.indexOf("\n") > -1) {
            TextView(context, null, 0, R.style.TextAppearance_App_Body1)
                .apply {
                    text = model.message
                    setPadding(context.resources.getDimensionPixelSize(R.dimen.page_margin))
                }
                .apply { builder.setView(this) }
        } else {
            builder.setMessage(model.message)
        }
        model.neutral?.apply {
            builder
                .setNeutralButton(strings.get(label), object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, p1: Int) {
                        action()
                        dialog?.dismiss()
                    }
                })
        }
        model.cancel?.also { button ->
            builder
                .setNegativeButton(strings.get(button.label), object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, p1: Int) {
                        model.cancel.let { button.action() }
                        dialog?.dismiss()
                    }
                })
        }
        model.dismiss?.also {
            builder.setOnDismissListener { it() }
        }
        return builder.create()
    }

    override fun createAndShowDialog(model: AlertDialogModel) {
        create(model).show()
    }

    override fun dismissDialog(dialogRef: Any) {
        (dialogRef as AlertDialog).dismiss()
    }
}
