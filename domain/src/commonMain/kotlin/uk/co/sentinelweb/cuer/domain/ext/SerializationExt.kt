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
import uk.co.sentinelweb.cuer.domain.backup.BackupFileModel
import uk.co.sentinelweb.cuer.domain.system.ErrorDomain
import uk.co.sentinelweb.cuer.domain.system.RequestDomain
import uk.co.sentinelweb.cuer.domain.system.ResponseDomain
import uk.co.sentinelweb.cuer.remote.server.message.messageSerializersModule

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

fun CategoryDomain.serialise() = domainJsonSerializer.encodeToString(
    CategoryDomain.serializer(), this
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

fun deserialisePlaylistConfig(input: String) = domainJsonSerializer.decodeFromString(PlaylistDomain.PlaylistConfigDomain.serializer(), input)

fun deserialiseImage(input: String) = domainJsonSerializer.decodeFromString(ImageDomain.serializer(), input)

fun deserialisePlaylistItem(input: String) = domainJsonSerializer.decodeFromString(PlaylistItemDomain.serializer(), input)

fun Identifier<GUID>.serialise() = domainJsonSerializer.encodeToString(Identifier.serializer(GUID.serializer()), this)

fun deserialiseGuidIdentifier(input: String) = domainJsonSerializer.decodeFromString(Identifier.serializer(GUID.serializer()), input)

fun GUID.serialise() = domainJsonSerializer.encodeToString(GUID.serializer(), this)

fun deserialiseGuid(input: String) = domainJsonSerializer.decodeFromString(GUID.serializer(), input)

// backup
fun BackupFileModel.serialise() = domainJsonSerializer.encodeToString(BackupFileModel.serializer(), this)

fun deserialiseBackupFileModel(input: String) = domainJsonSerializer.decodeFromString(BackupFileModel.serializer(), input)

// response
fun ResponseDomain.serialise() = domainJsonSerializer.encodeToString(ResponseDomain.serializer(), this)
fun RequestDomain.serialise() = domainJsonSerializer.encodeToString(RequestDomain.serializer(), this)

fun deserialiseResponse(input: String) = domainJsonSerializer.decodeFromString(ResponseDomain.serializer(), input)

fun deserialiseCategory(input: String) = domainJsonSerializer.decodeFromString(CategoryDomain.serializer(), input)

// AppDetailsDomain
fun List<AppDetailsDomain>.serialiseAppList() = domainJsonSerializer.encodeToString(ListSerializer(AppDetailsDomain.serializer()), this)

fun deserialiseAppList(input: String) = domainJsonSerializer.decodeFromString(ListSerializer(AppDetailsDomain.serializer()), input)

// LocalNodeDomain
fun LocalNodeDomain.serialise() = domainJsonSerializer.encodeToString(LocalNodeDomain.serializer(), this)

fun deserialiseLocalNode(input: String) = domainJsonSerializer.decodeFromString(LocalNodeDomain.serializer(), input)

// RemoteNodeDomain
fun RemoteNodeDomain.serialise() = domainJsonSerializer.encodeToString(RemoteNodeDomain.serializer(), this)

fun deserialiseRemoteNode(input: String) = domainJsonSerializer.decodeFromString(RemoteNodeDomain.serializer(), input)

fun List<RemoteNodeDomain>.serialise() = domainJsonSerializer.encodeToString(ListSerializer(RemoteNodeDomain.serializer()), this)

fun deserialiseRemoteNodeList(input: String) = domainJsonSerializer.decodeFromString(ListSerializer(RemoteNodeDomain.serializer()), input)


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

        LocalNodeDomain.AuthConfig.Username::class to LocalNodeDomain.AuthConfig.Username.serializer(),
        RemoteNodeDomain.AuthType.Username::class to RemoteNodeDomain.AuthType.Username.serializer(),
        RemoteNodeDomain.AuthType.Token::class to RemoteNodeDomain.AuthType.Token.serializer(),
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

    polymorphic(LocalNodeDomain.AuthConfig::class, LocalNodeDomain.AuthConfig.Open::class, LocalNodeDomain.AuthConfig.Open.serializer())
    polymorphic(LocalNodeDomain.AuthConfig::class, LocalNodeDomain.AuthConfig.Confirm::class, LocalNodeDomain.AuthConfig.Confirm.serializer())
    polymorphic(LocalNodeDomain.AuthConfig::class, LocalNodeDomain.AuthConfig.Username::class, LocalNodeDomain.AuthConfig.Username.serializer())
    polymorphic(RemoteNodeDomain.AuthType::class, RemoteNodeDomain.AuthType.Open::class, RemoteNodeDomain.AuthType.Open.serializer())
    polymorphic(RemoteNodeDomain.AuthType::class, RemoteNodeDomain.AuthType.Token::class, RemoteNodeDomain.AuthType.Token.serializer())
    polymorphic(RemoteNodeDomain.AuthType::class, RemoteNodeDomain.AuthType.Username::class, RemoteNodeDomain.AuthType.Username.serializer())

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


