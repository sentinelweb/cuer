package uk.co.sentinelweb.cuer.domain.ext

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus
import uk.co.sentinelweb.cuer.app.ui.onboarding.OnboardingContract
import uk.co.sentinelweb.cuer.domain.*
import uk.co.sentinelweb.cuer.domain.backup.BackupFileModel
import uk.co.sentinelweb.cuer.domain.serialization.InstantSerializer
import uk.co.sentinelweb.cuer.domain.serialization.LocalDateTimeSerializer
import uk.co.sentinelweb.cuer.domain.system.ErrorDomain
import uk.co.sentinelweb.cuer.domain.system.ResponseDomain

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

fun ResponseDomain.serialise() = domainJsonSerializer.encodeToString(
    ResponseDomain.serializer(), this
)

fun deserialiseResponse(input: String) = domainJsonSerializer.decodeFromString(
    ResponseDomain.serializer(), input
)

fun deserialiseCategory(input: String) = domainJsonSerializer.decodeFromString(
    CategoryDomain.serializer(), input
)

// AppDetailsDomain
fun List<AppDetailsDomain>.serialiseAppList() = domainJsonSerializer.encodeToString(
    ListSerializer(AppDetailsDomain.serializer()), this
)

fun deserialiseAppList(input: String) = domainJsonSerializer.decodeFromString(
    AppDetailsDomain.serializer(), input
)

fun OnboardingContract.Config.serialise() = domainJsonSerializer.encodeToString(
    OnboardingContract.Config.serializer(), this
)

fun deserialiseOnboarding(input: String) = domainJsonSerializer.decodeFromString(
    OnboardingContract.Config.serializer(), input
)

val domainJsonSerializer = Json {
    prettyPrint = true
    isLenient = true
    ignoreUnknownKeys = true
    classDiscriminator = "domainType"// property added when base domain type is use (see ResponseDomain)
    serializersModule = SerializersModule {
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
            ResponseDomain::class to ResponseDomain.serializer(),
            AppDetailsDomain::class to AppDetailsDomain.serializer(),
            OnboardingContract.Config::class to OnboardingContract.Config.serializer(),
        )
        polymorphic(Domain::class, PlaylistDomain::class, PlaylistDomain.serializer())
        polymorphic(Domain::class, MediaDomain::class, MediaDomain.serializer())
        polymorphic(Domain::class, ImageDomain::class, ImageDomain.serializer())
        polymorphic(Domain::class, ChannelDomain::class, ChannelDomain.serializer())
        polymorphic(Domain::class, PlaylistItemDomain::class, PlaylistItemDomain.serializer())
        polymorphic(Domain::class, PlaylistTreeDomain::class, PlaylistTreeDomain.serializer())
        polymorphic(Domain::class, SearchLocalDomain::class, SearchLocalDomain.serializer())
        polymorphic(Domain::class, SearchRemoteDomain::class, SearchRemoteDomain.serializer())
    }.plus(SerializersModule {
        contextual(Instant::class, InstantSerializer)
    }
    ).plus(SerializersModule {
        contextual(LocalDateTime::class, LocalDateTimeSerializer)
    }
    )
}
