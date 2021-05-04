package uk.co.sentinelweb.cuer.app.ui.common.dialog

import androidx.annotation.StringRes
import java.time.LocalDateTime

data class DatePickerDialogModel constructor(
    @StringRes override val title: Int,
    val date: LocalDateTime?,
    val confirm: (Long) -> Unit,
    val dismiss: () -> Unit
) : DialogModel(Type.DATE_PICKER, title)