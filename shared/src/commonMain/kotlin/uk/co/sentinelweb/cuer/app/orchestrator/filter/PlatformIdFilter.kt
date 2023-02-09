package uk.co.sentinelweb.cuer.app.orchestrator.filter

import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Operation
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source
import uk.co.sentinelweb.cuer.domain.*
import kotlin.reflect.KClass

class PlatformIdFilter(
    var targetSource: Source? = Source.LOCAL,
    var targetDomainClass: KClass<out Domain>? = null,
    var targetPlatformId: String? = null,
    var targetPlatform: PlatformDomain = PlatformDomain.YOUTUBE,
    private val targetOperation: List<Operation> = listOf(Operation.FLAT, Operation.FULL)
) {

    fun compareTo(each: Triple<Operation, Source, Domain>): Boolean {
        return targetOperation.contains(each.first)
                && targetSource == each.second
                && targetDomainClass?.let { comparePlatform(it, each.third) } ?: false
    }

    private fun comparePlatform(
        targetDomainClass: KClass<out Domain>,
        domain: Domain
    ): Boolean = if (targetDomainClass == domain::class) {
        when (domain) {
            is PlaylistDomain -> targetPlatformId == domain.platformId && targetPlatform == domain.platform
            is MediaDomain -> targetPlatformId == domain.platformId && targetPlatform == domain.platform
            is PlaylistItemDomain -> targetPlatformId == domain.media.platformId && targetPlatform == domain.media.platform
            else -> false
        }
    } else false
}
