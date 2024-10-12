import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Identifier.Locator
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.REMOTE
import uk.co.sentinelweb.cuer.domain.*

fun ChannelDomain.summarise(): String = """
    CHANNEL: id: $id, platform: $platform - $platformId, title: $title
""".trimIndent()

fun ChannelDomain.rewriteIdsToSource(source: Source, locator: Locator?) = this.copy(
    id = this.id?.copy(source = source, locator = locator)
)
