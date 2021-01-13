package uk.co.sentinelweb.cuer.domain.ext

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus
import uk.co.sentinelweb.cuer.domain.*
import uk.co.sentinelweb.cuer.domain.serialization.InstantSerializer
import uk.co.sentinelweb.cuer.domain.serialization.LocalDateTimeSerializer
import java.time.Instant
import java.time.LocalDateTime

fun ChannelDomain.serialise() = json.encodeToString(
    ChannelDomain.serializer(), this
)

fun MediaDomain.serialise() = json.encodeToString(
    MediaDomain.serializer(), this
)

fun List<MediaDomain>.serialiseList() = json.encodeToString(
    ListSerializer(MediaDomain.serializer()), this
)

fun PlaylistDomain.serialise() = json.encodeToString(
    PlaylistDomain.serializer(), this
)

fun PlaylistDomain.PlaylistConfigDomain.serialise() = json.encodeToString(
    PlaylistDomain.PlaylistConfigDomain.serializer(), this
)

fun ImageDomain.serialise() = json.encodeToString(
    ImageDomain.serializer(), this
)

fun PlaylistItemDomain.serialise() = json.encodeToString(
    PlaylistItemDomain.serializer(), this
)

fun deserialiseChannel(input: String) = json.decodeFromString(
    ChannelDomain.serializer(), input
)

fun deserialiseMedia(input: String) = json.decodeFromString(
    MediaDomain.serializer(), input
)

fun deserialiseMediaList(input: String) = json.decodeFromString(
    MediaDomain.serializer(), input
)

fun deserialisePlaylist(input: String) = json.decodeFromString(
    PlaylistDomain.serializer(), input
)

fun deserialisePlaylistList(input: String) = json.decodeFromString(
    PlaylistDomain.serializer(), input
)

fun deserialisePlaylistConfig(input: String) = json.decodeFromString(
    PlaylistDomain.PlaylistConfigDomain.serializer(), input
)

fun deserialiseImage(input: String) = json.decodeFromString(
    ImageDomain.serializer(), input
)

fun deserialisePlaylistItem(input: String) = json.decodeFromString(
    PlaylistItemDomain.serializer(), input
)

private val json = Json {
    prettyPrint = true
    serializersModule = SerializersModule {
        mapOf(
            PlaylistItemDomain::class to PlaylistItemDomain.serializer(),
            PlaylistDomain.PlaylistConfigDomain::class to PlaylistDomain.PlaylistConfigDomain.serializer(),
            PlaylistDomain::class to PlaylistDomain.serializer(),
            ChannelDomain::class to ChannelDomain.serializer(),
            MediaDomain::class to MediaDomain.serializer()
        )
    }.plus(SerializersModule {
        contextual(Instant::class, InstantSerializer)
    }
    ).plus(SerializersModule {
        contextual(LocalDateTime::class, LocalDateTimeSerializer)
    }
    )
}
