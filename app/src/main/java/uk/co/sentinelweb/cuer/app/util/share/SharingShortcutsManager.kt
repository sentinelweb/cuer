package uk.co.sentinelweb.cuer.app.util.share

import android.content.Context
import android.content.Intent
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.share.ShareActivity
import java.util.*

/**
 * RM: This adds icon menu items: possible items are:
 * - connect & play,
 * - next track, switch tv.
 * - paste/add
 *
 * SEE: https://developer.android.com/training/sharing/receive.html#providing-direct-share-targets
 *
 * Provides the Sharing Shortcuts items to the system.
 *
 * Use the ShortcutManagerCompat to make it work on older Android versions
 * without any extra work needed.
 *
 * Interactions with the ShortcutManager API can happen on any thread.
 */
class SharingShortcutsManager {
    /**
     * Publish the list of dynamics shortcuts that will be used in Direct Share.
     *
     *
     * For each shortcut, we specify the categories that it will be associated to,
     * the intent that will trigger when opened as a static launcher shortcut,
     * and the Shortcut ID between other things.
     *
     *
     * The Shortcut ID that we specify in the [ShortcutInfoCompat.Builder] constructor will
     * be received in the intent as [Intent.EXTRA_SHORTCUT_ID].
     *
     *
     * In this code sample, this method is completely static. We are always setting the same sharing
     * shortcuts. In a real-world example, we would replace existing shortcuts depending on
     * how the user interacts with the app as often as we want to.
     */
    fun pushDirectShareTargets(context: Context) {
        val shortcuts = ArrayList<ShortcutInfoCompat>()

        // The id passed in the constructor will become EXTRA_SHORTCUT_ID in the received Intent
        shortcuts.add(
            ShortcutInfoCompat.Builder(
                context,
                Integer.toString(1)
            )
                .setRank(1)
                .setShortLabel("Paste/Add") // Icon that will be displayed in the share target
                .setIcon(
                    IconCompat.createWithResource(
                        context,
                        R.drawable.ic_menu_paste_add_black
                    )
                )
                .setIntent(
                    ShareActivity.intent(
                        context,
                        paste = true
                    )
                ) // Make this sharing shortcut cached by the system
                .build()
        )

        ShortcutManagerCompat.addDynamicShortcuts(context, shortcuts)
    }

    /**
     * Remove all dynamic shortcuts
     */
    fun removeAllDirectShareTargets(context: Context) {
        ShortcutManagerCompat.removeAllDynamicShortcuts(context)
    }

    companion object {
        /**
         * Define maximum number of shortcuts.
         * Don't add more than [ShortcutManagerCompat.getMaxShortcutCountPerActivity].
         */
        private const val MAX_SHORTCUTS = 1
    }
}