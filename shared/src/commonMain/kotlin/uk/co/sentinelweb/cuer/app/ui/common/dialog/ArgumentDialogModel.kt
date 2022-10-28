package uk.co.sentinelweb.cuer.app.ui.common.dialog

data class ArgumentDialogModel constructor(
    override val type: Type,
    override val title: String,
    val args: Map<String, Any>
) : DialogModel(type, title)