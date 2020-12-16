package uk.co.sentinelweb.cuer.app.db.backup.version

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.modules.serializersModuleOf
import uk.co.sentinelweb.cuer.domain.ChannelDomain
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.serialization.InstantSerializer
import uk.co.sentinelweb.cuer.domain.serialization.LocalDateTimeSerializer
import java.time.Instant
import java.time.LocalDateTime

val jsonBackupSerialzer = Json(
    JsonConfiguration.Stable.copy(prettyPrint = true, isLenient = true, ignoreUnknownKeys = true, encodeDefaults = false),
    context = serializersModuleOf(
        mapOf(
            PlaylistItemDomain::class to PlaylistItemDomain.serializer(),
            PlaylistDomain.PlaylistConfigDomain::class to PlaylistDomain.PlaylistConfigDomain.serializer(),
            PlaylistDomain::class to PlaylistDomain.serializer(),
            ChannelDomain::class to ChannelDomain.serializer(),
            MediaDomain::class to MediaDomain.serializer(),
            Instant::class to InstantSerializer,
            LocalDateTime::class to LocalDateTimeSerializer
        )
    )
)