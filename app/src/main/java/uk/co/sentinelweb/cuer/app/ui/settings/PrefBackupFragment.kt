package uk.co.sentinelweb.cuer.app.ui.settings

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
import android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION
import android.os.Bundle
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
import uk.co.sentinelweb.cuer.app.ui.common.dialog.AlertDialogCreator
import uk.co.sentinelweb.cuer.app.ui.common.dialog.AlertDialogModel
import uk.co.sentinelweb.cuer.app.ui.main.MainActivity.Companion.TOP_LEVEL_DESTINATIONS
import uk.co.sentinelweb.cuer.app.util.extension.fragmentScopeWithSource
import uk.co.sentinelweb.cuer.app.util.wrapper.SnackbarWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

@Suppress("TooManyFunctions")
class PrefBackupFragment : PreferenceFragmentCompat(), PrefBackupContract.View, AndroidScopeComponent {

    override val scope: Scope by fragmentScopeWithSource<PrefBackupFragment>()
    private val presenter: PrefBackupContract.Presenter by inject()
    private val snackbarWrapper: SnackbarWrapper by inject()
    private val alertDialogCreator: AlertDialogCreator by inject()
    private val log: LogWrapper by inject()
    private lateinit var progress: ProgressBar

    init {
        log.tag(this)
    }

    private val autoSummary get() = findPreference(R.string.prefs_backup_summary_auto_key)
    private val autoClearPreference get() = findPreference(R.string.prefs_backup_clear_auto_key)
    private val autoSetPreference get() = findPreference(R.string.prefs_backup_set_auto_key)

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
        if (arguments?.getBoolean(AUTO_BACKUP) ?: false) {
            presenter.autoBackupDatabaseToJson()
            arguments?.remove(AUTO_BACKUP)
        }
        presenter.updateSummaryForAutoBackup()
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
            getString(R.string.prefs_backup_backup_key) -> presenter.manualBackupDatabaseToJson()
            getString(R.string.prefs_backup_restore_key) -> presenter.openRestoreFile()
            getString(R.string.prefs_backup_set_auto_key) -> presenter.onChooseAutoBackupFile()
            getString(R.string.prefs_backup_clear_auto_key) -> presenter.onClearAutoBackup()
        }
        return super.onPreferenceTreeClick(preference)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == CREATE_MANUAL_BACKUP_FILE) {
                data?.data?.also { uri ->
                    presenter.saveWriteData(uri.toString())
                }
            } else if (requestCode == OPEN_MANUAL_BACKUP_FILE) {
                data?.data?.also { uri ->
                    log.d("restore manual: $uri")
                    presenter.restoreFile(uri.toString())
                }
            } else if (requestCode == CREATE_AUTO_BACKUP_FILE) {
                data?.data?.also { uri ->
                    takePermissionForUri(data)
                    presenter.gotAutoBackupLocation(uri.toString())
                }
            } else if (requestCode == OPEN_AUTO_BACKUP_FILE) {
                data?.data?.also { uri ->
                    takePermissionForUri(data)
                    log.d("restore auto: $uri")
                    presenter.restoreAutoBackupLocation(uri.toString())
                }
            }
        }
    }

    @SuppressLint("WrongConstant")
    private fun takePermissionForUri(data: Intent) {
        val takeFlags =
            (FLAG_GRANT_READ_URI_PERMISSION or FLAG_GRANT_WRITE_URI_PERMISSION)
        requireActivity().contentResolver.takePersistableUriPermission(data.data!!, takeFlags)
    }

    override fun goBack() {
        findNavController().popBackStack()
    }

    override fun showProgress(b: Boolean) {
        progress.visibility = if (b) View.VISIBLE else View.INVISIBLE
    }

    override fun showMessage(msg: String) {
        snackbarWrapper.make(msg).show()
    }

    override fun showBackupError(message: String?) {
        AlertDialogModel(
            title = R.string.pref_backup_error_title,
            messageString = message,
            confirm = AlertDialogModel.Button(R.string.ok)
        ).apply {
            alertDialogCreator.create(this).show()
        }
    }

    // region manual-backup
    override fun openRestoreFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = BACKUP_MIME_TYPE
        }
        startActivityForResult(intent, OPEN_MANUAL_BACKUP_FILE)
    }

    override fun promptForSaveLocation(fileName: String) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = BACKUP_MIME_TYPE
            putExtra(Intent.EXTRA_TITLE, fileName)
        }
        startActivityForResult(intent, CREATE_MANUAL_BACKUP_FILE)
    }
    // endregion manual-backup


    // region auto-backup
    override fun askToRestoreAutoBackup() {
        AlertDialogModel(
            title = R.string.pref_backup_restore_auto_title,
            message = R.string.pref_backup_restore_auto_message, // says existing data will be lost
            confirm = AlertDialogModel.Button(
                label = R.string.pref_backup_restore_auto_confirm,
                action = { presenter.onConfirmRestoreAutoBackup() }
            ),
            cancel = AlertDialogModel.Button(label = R.string.cancel, action = {})
        ).apply {
            alertDialogCreator.create(this).show()
        }
    }

    override fun setAutoBackupValid(valid: Boolean) {
        autoSetPreference?.isVisible = !valid
    }

    override fun setAutoSummary(summary: String) {
        autoSummary?.summary = summary
    }

    override fun promptForCreateAutoBackupLocation(fileName: String) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = BACKUP_MIME_TYPE
            addFlags(FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(FLAG_GRANT_WRITE_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            putExtra(Intent.EXTRA_TITLE, fileName)
        }
        startActivityForResult(intent, CREATE_AUTO_BACKUP_FILE)
    }

    override fun promptForOpenAutoBackupLocation() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = BACKUP_MIME_TYPE
            addFlags(FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(FLAG_GRANT_WRITE_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }
        startActivityForResult(intent, OPEN_AUTO_BACKUP_FILE)
    }

    // endregion auto-backup

    companion object {
        private const val CREATE_MANUAL_BACKUP_FILE = 2
        private const val OPEN_MANUAL_BACKUP_FILE = 3
        private const val CREATE_AUTO_BACKUP_FILE = 4
        private const val OPEN_AUTO_BACKUP_FILE = 5
        private const val AUTO_BACKUP = "AUTO_BACKUP"
        private const val BACKUP_MIME_TYPE = "application/zip"

    }
}