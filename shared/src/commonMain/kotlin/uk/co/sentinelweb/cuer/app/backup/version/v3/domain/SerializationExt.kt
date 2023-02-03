package uk.co.sentinelweb.cuer.app.backup.version.v3.domain

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus
import uk.co.sentinelweb.cuer.domain.serialization.InstantSerializer
import uk.co.sentinelweb.cuer.domain.serialization.LocalDateTimeSerializer

// backup

fun deserialiseV3BackupFileModel(input: String) = v3JsonSerializer.decodeFromString(BackupFileModel.serializer(), input)

val v3SerializersModule = SerializersModule {
    mapOf(
        PlaylistItemDomain::class to PlaylistItemDomain.serializer(),
        PlaylistDomain.PlaylistConfigDomain::class to PlaylistDomain.PlaylistConfigDomain.serializer(),
        PlaylistDomain::class to PlaylistDomain.serializer(),
        ChannelDomain::class to ChannelDomain.serializer(),
        MediaDomain::class to MediaDomain.serializer(),
        BackupFileModel::class to BackupFileModel.serializer(),
    )
}.plus(SerializersModule {
    contextual(Instant::class, InstantSerializer)
}).plus(SerializersModule {
    contextual(LocalDateTime::class, LocalDateTimeSerializer)
})

val v3JsonSerializer = Json {
    prettyPrint = true
    isLenient = true
    ignoreUnknownKeys = true
    serializersModule = v3SerializersModule
}

//
//fun ChannelDomain.serialise() = v3JsonSerializer.encodeToString(
//    ChannelDomain.serializer(), this
//)
//
//fun MediaDomain.serialise() = v3JsonSerializer.encodeToString(
//    MediaDomain.serializer(), this
//)
//
//fun List<MediaDomain>.serialiseMedias() = v3JsonSerializer.encodeToString(
//    ListSerializer(MediaDomain.serializer()), this
//)
//
//fun PlaylistDomain.serialise() = v3JsonSerializer.encodeToString(
//    PlaylistDomain.serializer(), this
//)
//
//fun List<PlaylistDomain>.serialisePlaylists() = v3JsonSerializer.encodeToString(
//    ListSerializer(PlaylistDomain.serializer()), this
//)
//
//
//fun PlaylistDomain.PlaylistConfigDomain.serialise() = v3JsonSerializer.encodeToString(
//    PlaylistDomain.PlaylistConfigDomain.serializer(), this
//)
//
//fun ImageDomain.serialise() = v3JsonSerializer.encodeToString(
//    ImageDomain.serializer(), this
//)
//
//fun PlaylistItemDomain.serialise() = v3JsonSerializer.encodeToString(
//    PlaylistItemDomain.serializer(), this
//)
//
//fun deserialiseChannel(input: String) = v3JsonSerializer.decodeFromString(
//    ChannelDomain.serializer(), input
//)
//
//fun deserialiseMedia(input: String) = v3JsonSerializer.decodeFromString(
//    MediaDomain.serializer(), input
//)
//
//fun deserialiseMediaList(input: String) = v3JsonSerializer.decodeFromString(
//    MediaDomain.serializer(), input
//)
//
//fun deserialisePlaylist(input: String) = v3JsonSerializer.decodeFromString(
//    PlaylistDomain.serializer(), input
//)
//
//fun deserialisePlaylistList(input: String) = v3JsonSerializer.decodeFromString(
//    PlaylistDomain.serializer(), input
//)
//
//fun deserialisePlaylistConfig(input: String) = v3JsonSerializer.decodeFromString(PlaylistDomain.PlaylistConfigDomain.serializer(), input)
//
//fun deserialiseImage(input: String) = v3JsonSerializer.decodeFromString(ImageDomain.serializer(), input)
//
//fun deserialisePlaylistItem(input: String) = v3JsonSerializer.decodeFromString(PlaylistItemDomain.serializer(), input)


