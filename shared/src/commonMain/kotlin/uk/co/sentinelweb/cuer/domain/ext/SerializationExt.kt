package uk.co.sentinelweb.cuer.domain.ext

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus
import uk.co.sentinelweb.cuer.domain.*
import uk.co.sentinelweb.cuer.domain.backup.BackupFileModel
import uk.co.sentinelweb.cuer.domain.serialization.InstantSerializer
import uk.co.sentinelweb.cuer.domain.serialization.LocalDateTimeSerializer

//import java.time.Instant
//import java.time.LocalDateTime

fun ChannelDomain.serialise() = domainJsonSerializer.encodeToString(
    ChannelDomain.serializer(), this
)

fun MediaDomain.serialise() = domainJsonSerializer.encodeToString(
    MediaDomain.serializer(), this
)

fun List<MediaDomain>.serialiseMedias() = domainJsonSerializer.encodeToString(
    ListSerializer(MediaDomain.serializer()), this
)

fun PlaylistDomain.serialise() = domainJsonSerializer.encodeToString(
    PlaylistDomain.serializer(), this
)

fun List<PlaylistDomain>.serialisePlaylists() = domainJsonSerializer.encodeToString(
    ListSerializer(PlaylistDomain.serializer()), this
)


fun PlaylistDomain.PlaylistConfigDomain.serialise() = domainJsonSerializer.encodeToString(
    PlaylistDomain.PlaylistConfigDomain.serializer(), this
)

fun ImageDomain.serialise() = domainJsonSerializer.encodeToString(
    ImageDomain.serializer(), this
)

fun PlaylistItemDomain.serialise() = domainJsonSerializer.encodeToString(
    PlaylistItemDomain.serializer(), this
)

fun SearchLocalDomain.serialise() = domainJsonSerializer.encodeToString(
    SearchLocalDomain.serializer(), this
)

fun deserialiseSearchLocal(input: String) = domainJsonSerializer.decodeFromString(
    SearchLocalDomain.serializer(), input
)

fun SearchRemoteDomain.serialise() = domainJsonSerializer.encodeToString(
    SearchRemoteDomain.serializer(), this
)

fun deserialiseSearchRemote(input: String) = domainJsonSerializer.decodeFromString(
    SearchRemoteDomain.serializer(), input
)

fun deserialiseChannel(input: String) = domainJsonSerializer.decodeFromString(
    ChannelDomain.serializer(), input
)

fun deserialiseMedia(input: String) = domainJsonSerializer.decodeFromString(
    MediaDomain.serializer(), input
)

fun deserialiseMediaList(input: String) = domainJsonSerializer.decodeFromString(
    MediaDomain.serializer(), input
)

fun deserialisePlaylist(input: String) = domainJsonSerializer.decodeFromString(
    PlaylistDomain.serializer(), input
)

fun deserialisePlaylistList(input: String) = domainJsonSerializer.decodeFromString(
    PlaylistDomain.serializer(), input
)

fun deserialisePlaylistConfig(input: String) = domainJsonSerializer.decodeFromString(
    PlaylistDomain.PlaylistConfigDomain.serializer(), input
)

fun deserialiseImage(input: String) = domainJsonSerializer.decodeFromString(
    ImageDomain.serializer(), input
)

fun deserialisePlaylistItem(input: String) = domainJsonSerializer.decodeFromString(
    PlaylistItemDomain.serializer(), input
)

fun BackupFileModel.serialise() = domainJsonSerializer.encodeToString(
    BackupFileModel.serializer(), this
)

fun deserialiseBackupFileModel(input: String) = domainJsonSerializer.decodeFromString(
    BackupFileModel.serializer(), input
)

val domainJsonSerializer = Json {
    prettyPrint = true
    isLenient = true
    serializersModule = SerializersModule {
        mapOf(
            PlaylistItemDomain::class to PlaylistItemDomain.serializer(),
            PlaylistDomain.PlaylistConfigDomain::class to PlaylistDomain.PlaylistConfigDomain.serializer(),
            PlaylistDomain::class to PlaylistDomain.serializer(),
            ChannelDomain::class to ChannelDomain.serializer(),
            MediaDomain::class to MediaDomain.serializer(),
            SearchRemoteDomain to SearchRemoteDomain.serializer(),
            SearchLocalDomain to SearchLocalDomain.serializer(),
        )
    }.plus(SerializersModule {
        contextual(Instant::class, InstantSerializer)
    }
    ).plus(SerializersModule {
        contextual(LocalDateTime::class, LocalDateTimeSerializer)
    }
    )
}

