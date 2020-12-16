package uk.co.sentinelweb.cuer.domain.ext

import kotlinx.serialization.builtins.list
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.modules.serializersModuleOf
import uk.co.sentinelweb.cuer.domain.*
import uk.co.sentinelweb.cuer.domain.serialization.InstantSerializer
import uk.co.sentinelweb.cuer.domain.serialization.LocalDateTimeSerializer
import java.time.Instant
import java.time.LocalDateTime

fun ChannelDomain.serialise() = json.stringify(
    ChannelDomain.serializer(), this
)

fun MediaDomain.serialise() = json.stringify(
    MediaDomain.serializer(), this
)

fun List<MediaDomain>.serialiseList() = json.stringify(
    MediaDomain.serializer().list, this
)

fun PlaylistDomain.serialise() = json.stringify(
    PlaylistDomain.serializer(), this
)

fun PlaylistDomain.PlaylistConfigDomain.serialise() = json.stringify(
    PlaylistDomain.PlaylistConfigDomain.serializer(), this
)

fun ImageDomain.serialise() = json.stringify(
    ImageDomain.serializer(), this
)

fun PlaylistItemDomain.serialise() = json.stringify(
    PlaylistItemDomain.serializer(), this
)

fun deserialiseChannel(input: String) = json.parse(
    ChannelDomain.serializer(), input
)

fun deserialiseMedia(input: String) = json.parse(
    MediaDomain.serializer(), input
)

fun deserialiseMediaList(input: String) = json.parse(
    MediaDomain.serializer().list, input
)

fun deserialisePlaylist(input: String) = json.parse(
    PlaylistDomain.serializer(), input
)

fun deserialisePlaylistList(input: String) = json.parse(
    PlaylistDomain.serializer().list, input
)

fun deserialisePlaylistConfig(input: String) = json.parse(
    PlaylistDomain.PlaylistConfigDomain.serializer(), input
)

fun deserialiseImage(input: String) = json.parse(
    ImageDomain.serializer(), input
)

fun deserialisePlaylistItem(input: String) = json.parse(
    PlaylistItemDomain.serializer(), input
)

private val json = Json(
    JsonConfiguration.Stable.copy(prettyPrint = true),
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
