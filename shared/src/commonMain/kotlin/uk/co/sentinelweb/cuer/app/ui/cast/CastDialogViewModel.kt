package uk.co.sentinelweb.cuer.app.ui.cast

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.ui.remotes.selector.NodesDialogContract
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.domain.RemoteNodeDomain

class CastDialogViewModel(
    private val castController: CastController,
    private val remotesLauncher: NodesDialogContract.Launcher,
    private val coroutines: CoroutineContextProvider
) {
    private val _model = MutableStateFlow(CastDialogModel.blank)
    val model: Flow<CastDialogModel> = _model

    init {
        coroutines.mainScope.launch {
            _model.value = castController.map()
        }
    }

    fun connectCuerCast() {
        if (!_model.value.cuerCastStatus.isConnected) {
            remotesLauncher.launchRemotesDialog({ node, selectedScreen ->
                remotesLauncher.hideRemotesDialog()
                if (node is RemoteNodeDomain) {
                    castController.connectCuerCast(node, selectedScreen)
                }
            }, null)
        }
    }

    fun disconnectCuerCast() {
        castController.connectCuerCast(null, null)
        coroutines.mainScope.launch {
            _model.value = castController.map()
        }
    }

    fun stopCuerCast() {
        coroutines.mainScope.launch {
            castController.stopCuerCast()
            _model.value = castController.map()
        }
    }

    fun focusCuerCast() {
        coroutines.mainScope.launch {
            castController.focusCuerCast()
            _model.value = castController.map()
        }
    }

    fun connectChromeCast() {
        if (!_model.value.chromeCastStatus.isConnected) {
            castController.connectChromeCast()
            coroutines.mainScope.launch {
                _model.value = castController.map()
            }
        }
    }

    fun disconnectChromeCast() {
        castController.disonnectChromeCast()
        coroutines.mainScope.launch {
            _model.value = castController.map()
        }
    }
}
