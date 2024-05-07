package uk.co.sentinelweb.cuer.domain.ext

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.serializers.InstantIso8601Serializer
import kotlinx.datetime.serializers.LocalDateTimeIso8601Serializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Identifier
import uk.co.sentinelweb.cuer.domain.*
import uk.co.sentinelweb.cuer.domain.LocalNodeDomain.AuthConfig
import uk.co.sentinelweb.cuer.domain.RemoteNodeDomain.AuthType
import uk.co.sentinelweb.cuer.domain.backup.BackupFileModel
import uk.co.sentinelweb.cuer.domain.system.ErrorDomain
import uk.co.sentinelweb.cuer.domain.system.RequestDomain
import uk.co.sentinelweb.cuer.domain.system.ResponseDomain
import uk.co.sentinelweb.cuer.remote.server.messageSerializersModule

// channel
fun ChannelDomain.serialise() = domainJsonSerializer.encodeToString(ChannelDomain.serializer(), this)

fun deserialiseChannel(input: String) = domainJsonSerializer.decodeFromString(ChannelDomain.serializer(), input)

// media
fun MediaDomain.serialise() = domainJsonSerializer.encodeToString(MediaDomain.serializer(), this)

fun List<MediaDomain>.serialiseMedias() =
    domainJsonSerializer.encodeToString(ListSerializer(MediaDomain.serializer()), this)

fun deserialiseMedia(input: String) = domainJsonSerializer.decodeFromString(MediaDomain.serializer(), input)

fun deserialiseMediaList(input: String) = domainJsonSerializer.decodeFromString(MediaDomain.serializer(), input)

// playlist
fun PlaylistDomain.serialise() = domainJsonSerializer.encodeToString(
    PlaylistDomain.serializer(), this
)

fun List<PlaylistDomain>.serialisePlaylists() =
    domainJsonSerializer.encodeToString(ListSerializer(PlaylistDomain.serializer()), this)

fun PlaylistDomain.PlaylistConfigDomain.serialise() =
    domainJsonSerializer.encodeToString(PlaylistDomain.PlaylistConfigDomain.serializer(), this)

fun deserialisePlaylist(input: String) = domainJsonSerializer.decodeFromString(PlaylistDomain.serializer(), input)

fun deserialisePlaylistList(input: String) = domainJsonSerializer.decodeFromString(PlaylistDomain.serializer(), input)

fun deserialisePlaylistConfig(input: String) =
    domainJsonSerializer.decodeFromString(PlaylistDomain.PlaylistConfigDomain.serializer(), input)

// playlist item
fun PlaylistItemDomain.serialise() = domainJsonSerializer.encodeToString(PlaylistItemDomain.serializer(), this)

fun deserialisePlaylistItem(input: String) =
    domainJsonSerializer.decodeFromString(PlaylistItemDomain.serializer(), input)

// playlist AND item
fun PlaylistAndItemDomain.serialise() = domainJsonSerializer.encodeToString(PlaylistAndItemDomain.serializer(), this)

fun deserialisePlaylistAndItem(input: String) =
    domainJsonSerializer.decodeFromString(PlaylistAndItemDomain.serializer(), input)

// image
fun ImageDomain.serialise() = domainJsonSerializer.encodeToString(ImageDomain.serializer(), this)

fun deserialiseImage(input: String) = domainJsonSerializer.decodeFromString(ImageDomain.serializer(), input)

/// search
fun SearchLocalDomain.serialise() = domainJsonSerializer.encodeToString(SearchLocalDomain.serializer(), this)

fun deserialiseSearchLocal(input: String) = domainJsonSerializer.decodeFromString(SearchLocalDomain.serializer(), input)

fun SearchRemoteDomain.serialise() = domainJsonSerializer.encodeToString(SearchRemoteDomain.serializer(), this)

fun deserialiseSearchRemote(input: String) =
    domainJsonSerializer.decodeFromString(SearchRemoteDomain.serializer(), input)

// category
fun CategoryDomain.serialise() = domainJsonSerializer.encodeToString(CategoryDomain.serializer(), this)

fun Identifier<GUID>.serialise() = domainJsonSerializer.encodeToString(Identifier.serializer(GUID.serializer()), this)

fun deserialiseGuidIdentifier(input: String) =
    domainJsonSerializer.decodeFromString(Identifier.serializer(GUID.serializer()), input)

fun GUID.serialise() = domainJsonSerializer.encodeToString(GUID.serializer(), this)

fun deserialiseGuid(input: String) = domainJsonSerializer.decodeFromString(GUID.serializer(), input)

// backup
fun BackupFileModel.serialise() = domainJsonSerializer.encodeToString(BackupFileModel.serializer(), this)

fun deserialiseBackupFileModel(input: String) =
    domainJsonSerializer.decodeFromString(BackupFileModel.serializer(), input)

// response
fun ResponseDomain.serialise() = domainJsonSerializer.encodeToString(ResponseDomain.serializer(), this)
fun RequestDomain.serialise() = domainJsonSerializer.encodeToString(RequestDomain.serializer(), this)

fun deserialiseResponse(input: String) = domainJsonSerializer.decodeFromString(ResponseDomain.serializer(), input)

fun deserialiseCategory(input: String) = domainJsonSerializer.decodeFromString(CategoryDomain.serializer(), input)

// AppDetailsDomain
fun List<AppDetailsDomain>.serialiseAppList() =
    domainJsonSerializer.encodeToString(ListSerializer(AppDetailsDomain.serializer()), this)

fun deserialiseAppList(input: String) =
    domainJsonSerializer.decodeFromString(ListSerializer(AppDetailsDomain.serializer()), input)

// LocalNodeDomain
fun LocalNodeDomain.serialise() = domainJsonSerializer.encodeToString(LocalNodeDomain.serializer(), this)

fun deserialiseLocalNode(input: String) = domainJsonSerializer.decodeFromString(LocalNodeDomain.serializer(), input)

// RemoteNodeDomain
fun RemoteNodeDomain.serialise() = domainJsonSerializer.encodeToString(RemoteNodeDomain.serializer(), this)

fun deserialiseRemoteNode(input: String) = domainJsonSerializer.decodeFromString(RemoteNodeDomain.serializer(), input)

fun List<RemoteNodeDomain>.serialise() =
    domainJsonSerializer.encodeToString(ListSerializer(RemoteNodeDomain.serializer()), this)

fun deserialiseRemoteNodeList(input: String) =
    domainJsonSerializer.decodeFromString(ListSerializer(RemoteNodeDomain.serializer()), input)

fun deserialisePlaylistAndSubs(input: String) =
    domainJsonSerializer.decodeFromString(ListSerializer(PlaylistAndSubsDomain.serializer()), input)


val domainClassDiscriminator = "domainType"
val domainSerializersModule = SerializersModule {
    mapOf(
        PlaylistItemDomain::class to PlaylistItemDomain.serializer(),
        PlaylistDomain.PlaylistConfigDomain::class to PlaylistDomain.PlaylistConfigDomain.serializer(),
        PlaylistDomain::class to PlaylistDomain.serializer(),
        ChannelDomain::class to ChannelDomain.serializer(),
        MediaDomain::class to MediaDomain.serializer(),
        SearchRemoteDomain::class to SearchRemoteDomain.serializer(),
        SearchLocalDomain::class to SearchLocalDomain.serializer(),
        BackupFileModel::class to BackupFileModel.serializer(),
        ErrorDomain::class to ErrorDomain.serializer(),
        RequestDomain::class to RequestDomain.serializer(),
        ResponseDomain::class to ResponseDomain.serializer(),
        AppDetailsDomain::class to AppDetailsDomain.serializer(),
        LocalNodeDomain::class to LocalNodeDomain.serializer(),
        RemoteNodeDomain::class to RemoteNodeDomain.serializer(),
        PlaylistAndItemDomain::class to PlaylistAndItemDomain.serializer(),
        PlaylistAndSubsDomain::class to PlaylistAndSubsDomain.serializer(),

        AuthConfig.Username::class to AuthConfig.Username.serializer(),
        AuthType.Username::class to AuthType.Username.serializer(),
        AuthType.Token::class to AuthType.Token.serializer(),
    )
    polymorphic(Domain::class, PlaylistDomain::class, PlaylistDomain.serializer())
    polymorphic(Domain::class, MediaDomain::class, MediaDomain.serializer())
    polymorphic(Domain::class, ImageDomain::class, ImageDomain.serializer())
    polymorphic(Domain::class, ChannelDomain::class, ChannelDomain.serializer())
    polymorphic(Domain::class, PlaylistItemDomain::class, PlaylistItemDomain.serializer())
    polymorphic(Domain::class, PlaylistTreeDomain::class, PlaylistTreeDomain.serializer())
    polymorphic(Domain::class, SearchLocalDomain::class, SearchLocalDomain.serializer())
    polymorphic(Domain::class, SearchRemoteDomain::class, SearchRemoteDomain.serializer())
    polymorphic(Domain::class, LocalNodeDomain::class, LocalNodeDomain.serializer())
    polymorphic(Domain::class, RemoteNodeDomain::class, RemoteNodeDomain.serializer())
    polymorphic(Domain::class, PlaylistAndItemDomain::class, PlaylistAndItemDomain.serializer())
    polymorphic(Domain::class, PlaylistAndSubsDomain::class, PlaylistAndSubsDomain.serializer())

    polymorphic(AuthConfig::class, AuthConfig.Open::class, AuthConfig.Open.serializer())
    polymorphic(AuthConfig::class, AuthConfig.Confirm::class, AuthConfig.Confirm.serializer())
    polymorphic(AuthConfig::class, AuthConfig.Username::class, AuthConfig.Username.serializer())
    polymorphic(AuthType::class, AuthType.Open::class, AuthType.Open.serializer())
    polymorphic(AuthType::class, AuthType.Token::class, AuthType.Token.serializer())
    polymorphic(AuthType::class, AuthType.Username::class, AuthType.Username.serializer())

}.plus(SerializersModule {
    contextual(Instant::class, InstantIso8601Serializer)
}).plus(SerializersModule {
    contextual(LocalDateTime::class, LocalDateTimeIso8601Serializer)
})

val domainJsonSerializer = Json {
    prettyPrint = true
    isLenient = true
    ignoreUnknownKeys = true
    classDiscriminator = domainClassDiscriminator
    serializersModule = domainSerializersModule
}

val domainMessageJsonSerializer = Json {
    prettyPrint = true
    isLenient = true
    ignoreUnknownKeys = true
    classDiscriminator = domainClassDiscriminator//"classTypeDiscriminator"
    serializersModule = domainSerializersModule.plus(messageSerializersModule)
}


