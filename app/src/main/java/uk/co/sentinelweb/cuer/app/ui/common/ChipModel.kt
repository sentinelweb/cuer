package uk.co.sentinelweb.cuer.app.ui.common

sealed class ChipModel constructor(
    val type: Type,
    val text: String,
    val id: String?
) {
    enum class Type {
        TAG, CHANNEL, KEYWORD
    }
}