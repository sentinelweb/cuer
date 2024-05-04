package uk.co.sentinelweb.cuer.tools.ext

import com.appmattus.kotlinfixture.Fixture
import com.appmattus.kotlinfixture.decorator.nullability.NeverNullStrategy
import com.appmattus.kotlinfixture.decorator.nullability.nullabilityStrategy
import com.appmattus.kotlinfixture.decorator.optional.NeverOptionalStrategy
import com.appmattus.kotlinfixture.decorator.optional.optionalStrategy
import com.appmattus.kotlinfixture.kotlinFixture
import com.flextrade.jfixture.JFixture
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.orchestrator.toIdentifier
import uk.co.sentinelweb.cuer.domain.*
import uk.co.sentinelweb.cuer.domain.creator.GuidCreator

val kotlinFixtureDefaultConfig = kotlinFixture {
    nullabilityStrategy(NeverNullStrategy)
    repeatCount { 6 }
    factory<OrchestratorContract.Identifier<GUID>> {
        OrchestratorContract.Identifier(
            GuidCreator().create(),
            fixture()
        )
    }
    optionalStrategy(NeverOptionalStrategy) {
        propertyOverride(PlaylistDomain::id, NeverOptionalStrategy)
        propertyOverride(PlaylistItemDomain::id, NeverOptionalStrategy)
        propertyOverride(MediaDomain::id, NeverOptionalStrategy)
        propertyOverride(ImageDomain::id, NeverOptionalStrategy)
        propertyOverride(ChannelDomain::id, NeverOptionalStrategy)
    }
    factory<List<PlaylistItemDomain>> { (1..6).map { fixture<PlaylistItemDomain>() } }
}

private val fixture = kotlinFixtureDefaultConfig

// todo figure out how to share this file with test module sources
inline fun <reified T> JFixture.build() = this.create(T::class.java)

inline fun <reified S : Any, reified T : Collection<S>> JFixture.buildCollection(size: Int? = null) =
    size?.let { this.collections().createCollection(T::class.java, S::class.java, it) }
        ?: this.collections().createCollection(T::class.java, S::class.java)

// sometimes items is empty - which messes up the test
fun generatePlaylist(fixture: Fixture): PlaylistDomain {
    var fixPlaylist = fixture<PlaylistDomain>()
    while (fixPlaylist.items.size == 0) fixPlaylist = fixture()
    return fixPlaylist
}

fun guid() = GuidCreator().create()
fun id() = GuidCreator().create().toIdentifier(fixture())