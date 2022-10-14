package uk.co.sentinelweb.cuer.app.ui.common.ribbon

data class RibbonModel(
    val type: Type,
    val text: String,
    val icon: Int
) {
    enum class Type {
        LIKE, STAR, UNSTAR, SHARE, SUPPORT, COMMENT, LAUNCH, EDIT
    }
}