package uk.co.sentinelweb.cuer.app.ui.common.dialog

import androidx.core.util.Pair
import com.google.android.material.datepicker.MaterialDatePicker
import java.time.ZoneOffset

class DatePickerCreator {

    fun createDateRangePicker(model: DateRangePickerDialogModel): MaterialDatePicker<Pair<Long, Long>> {
        val picker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText(model.title)
            .setSelection(
                androidx.core.util.Pair(
                    model.fromDate?.toInstant(ZoneOffset.UTC)?.toEpochMilli(),
                    model.toDate?.toInstant(ZoneOffset.UTC)?.toEpochMilli()
                )
            )
            .build()
        picker.addOnPositiveButtonClickListener {
            model.confirm(it.first, it.second)
        }
        picker.addOnDismissListener { model.dismiss() }
        return picker
    }

    fun createDatePicker(model: DatePickerDialogModel): MaterialDatePicker<Long> {
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(model.title)
            .setSelection(model.date?.toInstant(ZoneOffset.UTC)?.toEpochMilli())
            .build()
        picker.addOnPositiveButtonClickListener {
            model.confirm(it)
        }
        picker.addOnDismissListener { model.dismiss() }
        return picker
    }
}