package uk.co.sentinelweb.cuer.app.ui.settings

interface PrefBackupContract {

    interface Presenter {
        fun backupDatabaseToJson()
        fun saveWriteData(uri: String)
        fun restoreFile(uriString: String)
    }

    interface View {
        fun promptForSaveLocation(fileName: String)
        fun showProgress(b: Boolean)
        fun showMessage(msg: String)
    }
}