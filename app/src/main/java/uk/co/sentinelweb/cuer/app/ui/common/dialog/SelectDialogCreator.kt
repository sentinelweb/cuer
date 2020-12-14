package uk.co.sentinelweb.cuer.app.ui.common.dialog

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import uk.co.sentinelweb.cuer.app.R

class SelectDialogCreator(
    private val context: Context
) {

    fun createMulti(
        model: SelectDialogModel
    ): AlertDialog {
        val builder = MaterialAlertDialogBuilder(context)
            .setTitle(model.title)
            .setMultiChoiceItems(
                model.items.map { it.name }.toTypedArray(),
                model.items.map { it.selected }.toBooleanArray(),
                object : DialogInterface.OnMultiChoiceClickListener {
                    override fun onClick(p0: DialogInterface?, which: Int, checked: Boolean) {
                        model.itemClick(which, checked)
                    }
                }
            )

        if (model.confirm != null) {
            builder
                .setPositiveButton(R.string.ok, object : DialogInterface.OnClickListener {
                    override fun onClick(p0: DialogInterface?, p1: Int) {
                        model.confirm?.let { it() }
                        p0?.dismiss()
                    }
                })
        }

        builder.setOnDismissListener { model.dismiss() }
        return builder.create()
    }

    fun createSingle(
        model: SelectDialogModel
    ): AlertDialog {
        val builder = MaterialAlertDialogBuilder(context)
            .setTitle(model.title)
            .setSingleChoiceItems(
                model.items.map { it.name }.toTypedArray(),
                model.items.indexOfFirst { it.selected },
                object : DialogInterface.OnClickListener {
                    override fun onClick(p0: DialogInterface?, which: Int) {
                        model.itemClick(which, true)
                        if (model.confirm == null) {
                            p0?.dismiss()
                        }
                    }
                }
            )

        if (model.confirm != null) {
            builder
                .setPositiveButton(R.string.ok, object : DialogInterface.OnClickListener {
                    override fun onClick(p0: DialogInterface?, p1: Int) {
                        model.confirm?.let { it() }
                        p0?.dismiss()
                    }
                })
        }
        builder.setOnDismissListener { model.dismiss() }
        return builder.create()
    }

}