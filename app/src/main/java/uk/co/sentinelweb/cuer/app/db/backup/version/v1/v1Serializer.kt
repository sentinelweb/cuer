package uk.co.sentinelweb.cuer.app.db.backup.version.v1

import kotlinx.serialization.builtins.list
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.modules.serializersModuleOf
import uk.co.sentinelweb.cuer.domain.serialization.InstantSerializer
import uk.co.sentinelweb.cuer.domain.serialization.LocalDateTimeSerializer
import java.time.Instant
import java.time.LocalDateTime

object v1Serializer {

    fun deserialiseMediaList(input: String) = jsonV1.parse(
        MediaDomain.serializer().list, input
    )

    val jsonV1 = Json(
        JsonConfiguration.Stable.copy(prettyPrint = true),
        context = serializersModuleOf(
            mapOf(
                ChannelDomain::class to ChannelDomain.serializer(),
                MediaDomain::class to MediaDomain.serializer(),
                Instant::class to InstantSerializer,
                LocalDateTime::class to LocalDateTimeSerializer
            )
        )
    )
}