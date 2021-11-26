package uk.co.sentinelweb.cuer.app.ui.common.dialog

import androidx.annotation.StringRes
import java.time.LocalDateTime

data class DateRangePickerDialogModel constructor(
    @StringRes override val title: Int,
    val fromDate: LocalDateTime?,
    val toDate: LocalDateTime?,
    val confirm: (Long, Long) -> Unit,
    val dismiss: () -> Unit
) : DialogModel(Type.DATE_RANGE_PICKER, title)