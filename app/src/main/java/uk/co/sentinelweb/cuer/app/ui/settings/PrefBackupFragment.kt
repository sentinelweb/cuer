package uk.co.sentinelweb.cuer.app.ui.settings

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.core.scope.Scope
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.main.MainActivity.Companion.TOP_LEVEL_DESTINATIONS
import uk.co.sentinelweb.cuer.app.util.extension.fragmentScopeWithSource
import uk.co.sentinelweb.cuer.app.util.wrapper.SnackbarWrapper
import java.io.File

class PrefBackupFragment constructor() : PreferenceFragmentCompat(), PrefBackupContract.View, AndroidScopeComponent {

    override val scope: Scope by fragmentScopeWithSource<PrefBackupFragment>()
    private val presenter: PrefBackupContract.Presenter by inject()
    private val snackbarWrapper: SnackbarWrapper by inject()
    private lateinit var progress: ProgressBar

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val onCreateView = super.onCreateView(inflater, container, savedInstanceState)
        (layoutInflater.inflate(R.layout.settings_toolbar, container, false) as Toolbar).also {
            (onCreateView as ViewGroup).addView(it, 0)
            //(activity as AppCompatActivity).setSupportActionBar(it)
            it.setupWithNavController(findNavController(), AppBarConfiguration(TOP_LEVEL_DESTINATIONS))
        }
        return onCreateView
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.pref_backup, rootKey)
    }

    override fun onStart() {
        super.onStart()
        checkToAddProgress()
    }

    private fun checkToAddProgress() {
        if (!::progress.isInitialized) {
            progress = LayoutInflater.from(activity)
                .inflate(R.layout.preference_progress, (view as ViewGroup), false) as ProgressBar
            (view as ViewGroup).addView(progress, 1)
        }
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        when (preference.key) {
            getString(R.string.prefs_backup_backup_key) -> presenter.backupDatabaseToJson()
            getString(R.string.prefs_backup_restore_key) -> presenter.openRestoreFile()
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

    override fun showProgress(b: Boolean) {
        progress.visibility = if (b) View.VISIBLE else View.INVISIBLE
    }

    override fun showMessage(msg: String) {
        snackbarWrapper.make(msg).show()
    }

    override fun openRestoreFile() {
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
        private const val BACKUP_MIME_TYPE = "application/zip"

    }
}