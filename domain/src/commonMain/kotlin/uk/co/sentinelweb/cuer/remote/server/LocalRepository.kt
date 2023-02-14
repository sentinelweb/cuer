package uk.co.sentinelweb.cuer.remote.server

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.db.repository.file.FileInteractor
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL_NETWORK
import uk.co.sentinelweb.cuer.app.orchestrator.toIdentifier
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.domain.BuildConfigDomain
import uk.co.sentinelweb.cuer.domain.LocalNodeDomain
import uk.co.sentinelweb.cuer.domain.creator.GuidCreator
import uk.co.sentinelweb.cuer.domain.ext.deserialiseLocalNode
import uk.co.sentinelweb.cuer.domain.ext.serialise
import uk.co.sentinelweb.cuer.remote.server.RemoteWebServerContract.Companion.WEB_SERVER_PORT_DEF

class LocalRepository(
    private val fileInteractor: FileInteractor,
    private val coroutineContext: CoroutineContextProvider,
    private val guidCreator: GuidCreator,
    private val buildConfigDomain: BuildConfigDomain,
) {

    private val _node: MutableStateFlow<LocalNodeDomain> = MutableStateFlow(getLocalNode())
    val node: Flow<LocalNodeDomain>
        get() = _node

    fun saveLocalNode(node: LocalNodeDomain) {
        coroutineContext.mainScope.launch {
            fileInteractor.saveJson(node.serialise())
            _node.emit(node)
        }
    }

    fun getLocalNode(): LocalNodeDomain =
        fileInteractor
            .takeIf { it.exists() }
            ?.loadJson()
            ?.takeIf { it.isNotEmpty() }
            ?.let { attemptDeserialize(it) }
            ?.copy(
                version = buildConfigDomain.version,
                versionCode = buildConfigDomain.versionCode
            )
            ?: run {
                LocalNodeDomain(
                    id = guidCreator.create().toIdentifier(LOCAL_NETWORK),
                    ipAddress = "",
                    port = WEB_SERVER_PORT_DEF,
                    deviceType = buildConfigDomain.deviceType,
                    device = buildConfigDomain.device,
                    version = buildConfigDomain.version,
                    versionCode = buildConfigDomain.versionCode,
                ).also { saveLocalNode(it) }
            }

    private fun attemptDeserialize(it: String): LocalNodeDomain? = try {
        deserialiseLocalNode(it)
    } catch (e: Exception) {
        null
    }
}
