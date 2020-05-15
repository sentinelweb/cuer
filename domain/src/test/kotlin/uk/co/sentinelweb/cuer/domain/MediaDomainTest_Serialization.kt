package uk.co.sentinelweb.cuer.domain

import com.flextrade.jfixture.FixtureAnnotations
import com.flextrade.jfixture.annotations.Fixture
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.modules.serializersModuleOf
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
        val json = Json(
            JsonConfiguration.Stable.copy(prettyPrint = true),
            context = serializersModuleOf(
                mapOf(
                    MediaDomain::class to MediaDomain.serializer(),
                    Instant::class to InstantSerializer,
                    LocalDateTime::class to LocalDateTimeSerializer
                )
            )
        )
        val serialized = json.stringify(MediaDomain.serializer(), media)

        val deserializedMedia = json.parse(MediaDomain.serializer(), serialized)

        assertEquals(media, deserializedMedia)
    }
}