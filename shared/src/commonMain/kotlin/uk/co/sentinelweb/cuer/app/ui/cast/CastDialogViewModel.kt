package uk.co.sentinelweb.cuer.app.ui.cast

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class CastDialogViewModel(
    private val castController: CastController
) {
    private val _model = MutableStateFlow(CastDialogModel.blank)
    val model: Flow<CastDialogModel> = _model


    fun connectCuerCast() {
        // show remote nodes
    }

    fun disconnectCuerCast() {
        // disconnect
        castController.connectCuerCast(null)
    }

    fun connectChromeCast() {
        // show cast dialog
    }

    fun disconnectChromeCast() {
        // disconnect
    }


}