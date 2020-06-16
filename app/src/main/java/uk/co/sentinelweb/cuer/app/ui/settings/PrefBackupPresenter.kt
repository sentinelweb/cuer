package uk.co.sentinelweb.cuer.app.ui.settings

import uk.co.sentinelweb.cuer.app.util.wrapper.ToastWrapper

class PrefBackupPresenter constructor(
    private val view: PrefBackupContract.View,
    private val toastWrapper: ToastWrapper

) : PrefBackupContract.Presenter {

    override fun backupDatabaseToJson() {
        toastWrapper.show("JSON backup")
    }
}