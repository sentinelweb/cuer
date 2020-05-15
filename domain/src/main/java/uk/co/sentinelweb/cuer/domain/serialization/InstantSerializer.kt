package uk.co.sentinelweb.cuer.domain.serialization

import kotlinx.serialization.*
import java.time.Instant

@Serializer(forClass = Instant::class)
object InstantSerializer : KSerializer<Instant> {

    override val descriptor: SerialDescriptor =
        PrimitiveDescriptor("WithCustomDefault", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): Instant {
        return Instant.parse(decoder.decodeString())
    }
}