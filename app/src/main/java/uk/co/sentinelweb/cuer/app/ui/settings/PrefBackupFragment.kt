package uk.co.sentinelweb.cuer.app.ui.settings

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
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
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import java.io.File

@Suppress("TooManyFunctions")
class PrefBackupFragment : PreferenceFragmentCompat(), PrefBackupContract.View, AndroidScopeComponent {

    override val scope: Scope by fragmentScopeWithSource<PrefBackupFragment>()
    private val presenter: PrefBackupContract.Presenter by inject()
    private val snackbarWrapper: SnackbarWrapper by inject()
    private val log: LogWrapper by inject()
    private lateinit var progress: ProgressBar

    init {
        log.tag(this)
    }

    private val autoSummary get() = findPreference(R.string.prefs_backup_summary_auto_key)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
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
        if (arguments?.getBoolean("AUTO_BACKUP") ?: false) {
            presenter.autoBackupDatabaseToJson()
            arguments?.remove("AUTO_BACKUP")
        }
        presenter.buildAutoSummary()

    }

    private fun checkToAddProgress() {
        if (!::progress.isInitialized) {
            progress = LayoutInflater.from(activity)
                .inflate(R.layout.preference_progress, (view as ViewGroup), false) as ProgressBar
            (view as ViewGroup).addView(progress, 1)
        }
    }

    override fun setAutoSummary(summary: String) {
        autoSummary?.summary = summary
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        when (preference.key) {
            getString(R.string.prefs_backup_backup_key) -> presenter.manualBackupDatabaseToJson()
            getString(R.string.prefs_backup_restore_key) -> presenter.openRestoreFile()
            getString(R.string.prefs_backup_clear_auto_key) -> presenter.clearAutoBackup()
        }

        return super.onPreferenceTreeClick(preference)
    }

    @SuppressLint("WrongConstant")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == CREATE_FILE) {
                data?.data?.also { uri ->
                    presenter.saveWriteData(uri.toString())
                }
            } else if (requestCode == AUTO_BACKUP_FILE) {
                data?.data?.also { uri ->
                    val takeFlags = (data.flags and
                            (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION))
                    log.d("takeFlags: $takeFlags")
                    val resolver: ContentResolver = requireActivity().contentResolver
                    resolver.takePersistableUriPermission(uri, takeFlags)
                    presenter.gotAutoBackupLocation(uri.toString())
                }
            } else if (requestCode == READ_FILE) {
                data?.data?.also { uri ->
                    presenter.restoreFile(uri.toString())
                }
            }
        }
    }

    override fun goBack() {
        findNavController().popBackStack()
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

    override fun promptForAutoBackupLocation(fileName: String) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = BACKUP_MIME_TYPE
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            putExtra(Intent.EXTRA_TITLE, fileName)
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, initialUri())
        }
        startActivityForResult(intent, AUTO_BACKUP_FILE)
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
        private const val AUTO_BACKUP_FILE = 4
        private const val READ_FILE = 3
        private const val BACKUP_MIME_TYPE = "application/zip"
    }
}