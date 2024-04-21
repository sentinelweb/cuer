package uk.co.sentinelweb.cuer.remote.server

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import uk.co.sentinelweb.cuer.app.db.repository.file.JsonFileInteractor
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL_NETWORK
import uk.co.sentinelweb.cuer.app.orchestrator.toIdentifier
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.core.wrapper.WifiStateProvider
import uk.co.sentinelweb.cuer.domain.BuildConfigDomain
import uk.co.sentinelweb.cuer.domain.LocalNodeDomain
import uk.co.sentinelweb.cuer.domain.creator.GuidCreator
import uk.co.sentinelweb.cuer.domain.ext.deserialiseLocalNode
import uk.co.sentinelweb.cuer.domain.ext.serialise
import uk.co.sentinelweb.cuer.remote.server.RemoteWebServerContract.Companion.WEB_SERVER_PORT_DEF

class LocalRepository(
    private val jsonFileInteractor: JsonFileInteractor,
    private val coroutineContext: CoroutineContextProvider,
    private val guidCreator: GuidCreator,
    private val buildConfigDomain: BuildConfigDomain,
    private val log: LogWrapper,
) : KoinComponent {

    private val wifiStateProvider: WifiStateProvider by inject()

    private val _nodeFlow: MutableStateFlow<LocalNodeDomain>

    val updatesFlow: Flow<LocalNodeDomain>
        get() = _nodeFlow

    init {
        log.tag(this)
        _nodeFlow = MutableStateFlow(loadLocalNode())
    }

    val localNode
        get() = _nodeFlow.value.nodeUpdated()

    private fun LocalNodeDomain.nodeUpdated() = this
        .let { node -> wifiStateProvider.wifiState.let { node.copy(ipAddress = it.ip ?: "") } }
        .also { saveLocalNode(it) }

    fun saveLocalNode(node: LocalNodeDomain) {
        if (_nodeFlow.value != node) {
            coroutineContext.mainScope.launch {
                jsonFileInteractor.saveJson(node.serialise())
                _nodeFlow.emit(node)
            }
        }
    }

    private fun loadLocalNode(): LocalNodeDomain =
        jsonFileInteractor
            .takeIf { it.exists() }
            ?.loadJson()
            ?.takeIf { it.isNotEmpty() }
            ?.let { attemptDeserialize(it) }
            ?.copy(
                version = buildConfigDomain.version,
                versionCode = buildConfigDomain.versionCode,
                deviceType = buildConfigDomain.deviceType,
                device = buildConfigDomain.device,
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
                )
            }

    private fun attemptDeserialize(it: String): LocalNodeDomain? = try {
        deserialiseLocalNode(it)
    } catch (e: Exception) {
        null
    }
}
