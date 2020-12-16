package uk.co.sentinelweb.cuer.app.ui.common.dialog

data class AlertDialogModel constructor(
    override val title: String,
    val message: String,
    val confirmAction: () -> Unit
) : DialogModel(Type.CONFIRM, title)