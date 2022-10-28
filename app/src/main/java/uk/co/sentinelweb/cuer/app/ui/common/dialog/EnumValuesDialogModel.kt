package uk.co.sentinelweb.cuer.app.ui.common.dialog

data class EnumValuesDialogModel<E : Enum<E>> constructor(
    override val title: String,
    val values: List<E>,
    val selected: E,
    val select: (E) -> Unit,
    val dismiss: () -> Unit
) : DialogModel(Type.ENUM_VALUES, title)