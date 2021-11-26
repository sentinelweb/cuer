package uk.co.sentinelweb.cuer.app.ui.common.dialog

import androidx.annotation.StringRes

data class EnumValuesDialogModel<E : Enum<E>> constructor(
    @StringRes override val title: Int,
    val values: List<E>,
    val selected: E,
    val select: (E) -> Unit,
    val dismiss: () -> Unit
) : DialogModel(Type.ENUM_VALUES, title)