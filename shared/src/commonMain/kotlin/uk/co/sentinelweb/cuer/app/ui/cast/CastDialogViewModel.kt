package uk.co.sentinelweb.cuer.app.ui.cast

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.ui.remotes.selector.RemotesDialogContract
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider

class CastDialogViewModel(
    private val castController: CastController,
    private val remotesLauncher: RemotesDialogContract.Launcher,
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
            remotesLauncher.launchRemotesDialog { remoteNode, selectedScreen ->
                remotesLauncher.hideRemotesDialog()
                castController.connectCuerCast(remoteNode)
            }
        }
    }

    fun disconnectCuerCast() {
        castController.connectCuerCast(null)
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