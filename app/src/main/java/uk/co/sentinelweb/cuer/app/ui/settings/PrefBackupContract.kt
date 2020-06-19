package uk.co.sentinelweb.cuer.app.ui.settings

interface PrefBackupContract {

    interface Presenter {
        fun backupDatabaseToJson()
        fun saveWriteData(uri: String)
    }

    interface View {
        fun promptForSaveLocation(fileName: String)
    }
}