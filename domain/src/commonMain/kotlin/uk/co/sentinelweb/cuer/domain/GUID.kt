package uk.co.sentinelweb.cuer.domain

import kotlinx.serialization.Serializable

@Serializable
data class GUID constructor(val value: String)

fun String.toGUID(): GUID = GUID(this)