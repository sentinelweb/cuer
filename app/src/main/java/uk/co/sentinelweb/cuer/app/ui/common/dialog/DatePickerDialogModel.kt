package uk.co.sentinelweb.cuer.app.ui.common.dialog

import java.time.LocalDateTime

data class DatePickerDialogModel constructor(
    override val title: String,
    val date: LocalDateTime?,
    val confirm: (Long) -> Unit,
    val dismiss: () -> Unit
) : DialogModel(Type.DATE_PICKER, title)