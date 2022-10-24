package uk.co.sentinelweb.cuer.app.ui.common.dialog

import androidx.annotation.StringRes

data class AlertDialogModel constructor(
    @StringRes override val title: Int,
    @StringRes val message: Int = 0,
    var messageString: String? = null,
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