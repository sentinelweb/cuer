package uk.co.sentinelweb.cuer.app.ui.common.dialog

import androidx.annotation.StringRes

data class SelectDialogModel constructor(
    override val type: Type,
    @StringRes override val title: Int,
    val multi: Boolean,
    val items: List<Item> = listOf(),
    val itemClick: (Int, Boolean) -> Unit,
    val confirm: (() -> Unit)?,
    val dismiss: () -> Unit
) : DialogModel(type, title) {

    data class Item constructor(
        val name: String,
        val selected: Boolean = false,
        val selectable: Boolean = false
    )
}