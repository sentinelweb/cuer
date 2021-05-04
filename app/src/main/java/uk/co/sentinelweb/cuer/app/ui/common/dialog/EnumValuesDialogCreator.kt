package uk.co.sentinelweb.cuer.app.ui.common.dialog

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class EnumValuesDialogCreator(
    private val context: Context
) {

    fun <E : Enum<E>> create(
        model: EnumValuesDialogModel<E>
    ): AlertDialog {
        val builder = MaterialAlertDialogBuilder(context)
            .setTitle(model.title)
            .setSingleChoiceItems(
                model.values.map { it.name }.toTypedArray(),
                model.values.indexOfFirst { it == model.selected },
                object : DialogInterface.OnClickListener {
                    override fun onClick(p0: DialogInterface?, which: Int) {
                        model.select(model.values[which])
                        p0?.dismiss()
                    }
                }
            )
        builder.setOnDismissListener { model.dismiss() }
        return builder.create()
    }
}