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
        _model.value = castController.map()
    }

    fun connectCuerCast() {
        remotesLauncher.launchRemotesDialog { remoteNode ->
            // todo screen selection
            remotesLauncher.hideRemotesDialog()
            castController.connectCuerCast(remoteNode)
        }
    }

    fun disconnectCuerCast() {
        castController.connectCuerCast(null)
        _model.value = castController.map()
    }

    fun stopCuerCast() {
        coroutines.mainScope.launch {
            castController.stopCuerCast()
            _model.value = castController.map()
        }
    }

    fun connectChromeCast() {
        castController.connectChromeCast()
        _model.value = castController.map()
    }

    fun disconnectChromeCast() {
        castController.disonnectChromeCast()
        _model.value = castController.map()
    }
}