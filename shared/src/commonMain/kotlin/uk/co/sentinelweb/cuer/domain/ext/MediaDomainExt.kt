package uk.co.sentinelweb.cuer.domain.ext

import uk.co.sentinelweb.cuer.domain.MediaDomain

fun MediaDomain.stringMedia(): String? = "id=$id title=$title platrform=$platform platformId=$platformId"

fun MediaDomain.isLiveOrUpcoming() = isLiveBroadcastUpcoming || isLiveBroadcast