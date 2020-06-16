package uk.co.sentinelweb.cuer.app.ui.settings

interface PrefBackupContract {
    interface Presenter {
        fun backupDatabaseToJson()
    }

    interface View
}