package uk.co.sentinelweb.cuer.app.ui.common.ktx

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaLocalDateTime
import java.time.OffsetDateTime

fun LocalDateTime.convertToLocalMillis() =
    toJavaLocalDateTime().toInstant(OffsetDateTime.now().getOffset())
        .toEpochMilli()