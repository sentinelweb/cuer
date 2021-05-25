package uk.co.sentinelweb.cuer.domain

import com.flextrade.jfixture.FixtureAnnotations
import com.flextrade.jfixture.annotations.Fixture
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import uk.co.sentinelweb.cuer.domain.serialization.InstantSerializer
import uk.co.sentinelweb.cuer.domain.serialization.LocalDateTimeSerializer
import java.time.Instant
import java.time.LocalDateTime

class MediaDomainTest_Serialization {

    @Fixture
    private lateinit var media: MediaDomain

    @Before
    fun setUp() {
        FixtureAnnotations.initFixtures(this)
    }

    @Test
    fun serialization() {
        val json = Json {
            prettyPrint = true
            serializersModule = SerializersModule {
                mapOf(
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
        val serialized = json.encodeToString(MediaDomain.serializer(), media)

        val deserializedMedia = json.decodeFromString(MediaDomain.serializer(), serialized)

        assertEquals(media, deserializedMedia)
    }
}