package uk.co.sentinelweb.cuer.remote.server

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.serializers.InstantIso8601Serializer
import kotlinx.datetime.serializers.LocalDateTimeIso8601Serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus
import uk.co.sentinelweb.cuer.domain.ext.domainClassDiscriminator
import uk.co.sentinelweb.cuer.remote.server.message.AvailableMessage
import uk.co.sentinelweb.cuer.remote.server.message.RequestMessage
import uk.co.sentinelweb.cuer.remote.server.player.PlayerSessionContract

interface Message {
}

// region JSON serializer
///////////////////////////////////////////////////////////////////////////
fun AvailableMessage.serialise() = messageJsonSerializer.encodeToString(AvailableMessage.serializer(), this)
fun deserialiseMulti(json: String) = messageJsonSerializer.decodeFromString(AvailableMessage.serializer(), json)
fun PlayerSessionContract.PlayerCommandMessage.serialise() =
    messageJsonSerializer.encodeToString(PlayerSessionContract.PlayerCommandMessage.serializer(), this)

fun deserialisePlayer(json: String) =
    messageJsonSerializer.decodeFromString(PlayerSessionContract.PlayerCommandMessage.serializer(), json)

fun RequestMessage.serialise() = messageJsonSerializer.encodeToString(RequestMessage.serializer(), this)

val messageSerializersModule = SerializersModule {
    mapOf(
        AvailableMessage::class to AvailableMessage.serializer(),
        RequestMessage::class to RequestMessage.serializer(),
        PlayerSessionContract.PlayerCommandMessage::class to PlayerSessionContract.PlayerCommandMessage.serializer(),
        PlayerSessionContract.PlayerStatusMessage::class to PlayerSessionContract.PlayerStatusMessage.serializer(),
    )
    polymorphic(Message::class, AvailableMessage::class, AvailableMessage.serializer())
    polymorphic(Message::class, RequestMessage::class, RequestMessage.serializer())
    polymorphic(Message::class, RequestMessage::class, RequestMessage.serializer())

}.plus(SerializersModule {
    contextual(Instant::class, InstantIso8601Serializer)
}).plus(SerializersModule {
    contextual(LocalDateTime::class, LocalDateTimeIso8601Serializer)
})

val messageJsonSerializer = Json {
    prettyPrint = true
    isLenient = true
    ignoreUnknownKeys = true
    classDiscriminator = domainClassDiscriminator
    serializersModule = messageSerializersModule
}
//endregion