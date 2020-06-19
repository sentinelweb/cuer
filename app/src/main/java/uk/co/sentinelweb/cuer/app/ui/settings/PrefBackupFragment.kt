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
            getString(R.string.pref_key_restore) -> presenter.backupDatabaseToJson()
        }

        return super.onPreferenceTreeClick(preference)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CREATE_FILE
            && resultCode == Activity.RESULT_OK
        ) {
            data?.data?.also { uri ->
                presenter.saveWriteData(uri.toString())
            }
        }
    }

    override fun promptForSaveLocation(fileName: String) {
        val pickerInitialUri = this.activity?.getExternalFilesDir(null)
            ?.apply { Uri.fromFile(File(this.absolutePath)) }
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
            putExtra(Intent.EXTRA_TITLE, fileName)

            // Optionally, specify a URI for the directory that should be opened in
            // the system file picker before your app creates the document.
            pickerInitialUri?.let {
                putExtra(DocumentsContract.EXTRA_INITIAL_URI, it)
            }
        }
        startActivityForResult(intent, CREATE_FILE)
    }

    companion object {
        private const val CREATE_FILE = 2

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