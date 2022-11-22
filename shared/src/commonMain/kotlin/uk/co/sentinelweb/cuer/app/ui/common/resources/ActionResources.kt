package uk.co.sentinelweb.cuer.app.ui.common.resources

import kotlinx.serialization.Serializable

@Serializable
data class ActionResources(
    val label: String? = null,
    val icon: Int? = null,
    val color: Int? = null,
)