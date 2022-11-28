package uk.co.sentinelweb.cuer.app.ui.common.dialog

import uk.co.sentinelweb.cuer.app.ui.common.resources.StringResource

data class AlertDialogModel constructor(
    override val title: String,
    var message: String,
    val confirm: Button,
    val neutral: Button? = null,
    val cancel: Button? = null,
    val dismiss: (() -> Unit)? = null
) : DialogModel(Type.CONFIRM, title) {

    data class Button(
        val label: StringResource,
        val action: () -> Unit = {}
    )
}