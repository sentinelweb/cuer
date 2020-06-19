package uk.co.sentinelweb.cuer.app.ui.settings

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import org.koin.android.scope.currentScope
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.R
import java.io.File

class PrefBackupFragment constructor() : PreferenceFragmentCompat(), PrefBackupContract.View {

    private val presenter: PrefBackupContract.Presenter by currentScope.inject()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.pref_backup, rootKey)
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        when (preference.key) {
            getString(R.string.pref_key_backup) -> presenter.backupDatabaseToJson()
            getString(R.string.pref_key_restore) -> openFile()
        }

        return super.onPreferenceTreeClick(preference)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == CREATE_FILE) {
                data?.data?.also { uri ->
                    presenter.saveWriteData(uri.toString())
                }
            } else if (requestCode == READ_FILE) {
                data?.data?.also { uri ->
                    presenter.restoreFile(uri.toString())
                }
            }
        }
    }

    override fun promptForSaveLocation(fileName: String) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = BACKUP_MIME_TYPE
            putExtra(Intent.EXTRA_TITLE, fileName)
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, initialUri())
        }
        startActivityForResult(intent, CREATE_FILE)
    }

    private fun openFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = BACKUP_MIME_TYPE
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, initialUri())
        }
        startActivityForResult(intent, READ_FILE)
    }

    private fun initialUri() = this.activity
        ?.getExternalFilesDir(null)
        ?.apply { Uri.fromFile(File(this.absolutePath)) }

    companion object {
        private const val CREATE_FILE = 2
        private const val READ_FILE = 3
        private const val BACKUP_MIME_TYPE = "application/json"

        @JvmStatic
        val fragmentModule = module {
            scope(named<PrefBackupFragment>()) {
                scoped<PrefBackupContract.View> { getSource() }
                scoped<PrefBackupContract.Presenter> {
                    PrefBackupPresenter(
                        view = get(),
                        state = get(),
                        toastWrapper = get(),
                        backupManager = get(),
                        timeProvider = get(),
                        fileWrapper = get()
                    )
                }
                viewModel { PrefBackupState() }
            }
        }

    }
}