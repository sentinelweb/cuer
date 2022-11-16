package uk.co.sentinelweb.cuer.domain

import kotlinx.serialization.Serializable

@Serializable
open class AppDetailsDomain(
    open val title: CharSequence,
    open val appId: String
)
