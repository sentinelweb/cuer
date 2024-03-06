package uk.co.sentinelweb.cuer.tools.ext

import com.appmattus.kotlinfixture.Fixture
import com.appmattus.kotlinfixture.decorator.nullability.NeverNullStrategy
import com.appmattus.kotlinfixture.decorator.nullability.nullabilityStrategy
import com.appmattus.kotlinfixture.decorator.optional.NeverOptionalStrategy
import com.appmattus.kotlinfixture.decorator.optional.optionalStrategy
import com.appmattus.kotlinfixture.kotlinFixture
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.domain.*
import uk.co.sentinelweb.cuer.domain.creator.GuidCreator

val kotlinFixtureDefaultConfig = kotlinFixture {
    nullabilityStrategy(NeverNullStrategy)
    repeatCount { 6 }
    factory { OrchestratorContract.Identifier(GuidCreator().create(), fixture()) }
    optionalStrategy(NeverOptionalStrategy) {
        propertyOverride(PlaylistDomain::id, NeverOptionalStrategy)
        propertyOverride(PlaylistItemDomain::id, NeverOptionalStrategy)
        propertyOverride(MediaDomain::id, NeverOptionalStrategy)
        propertyOverride(ImageDomain::id, NeverOptionalStrategy)
        propertyOverride(ChannelDomain::id, NeverOptionalStrategy)
    }
}

fun generatePlaylist(fixture: Fixture): PlaylistDomain {
    var fixPlaylist = fixture<PlaylistDomain>()
    while (fixPlaylist.items.size == 0) fixPlaylist = fixture()
    return fixPlaylist
}
