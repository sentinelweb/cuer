package uk.co.sentinelweb.cuer.app.ui.common.dialog

import androidx.annotation.StringRes

data class AlertDialogModel constructor(
    override val title: String,
    var message: String,
    val confirm: Button,
    val neutral: Button? = null,
    val cancel: Button? = null,
    val dismiss: (() -> Unit)? = null
) : DialogModel(Type.CONFIRM, title) {

    data class Button(
        @StringRes val label: Int,
        val action: () -> Unit = {}
    )
}