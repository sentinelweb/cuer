package uk.co.sentinelweb.cuer.app.ui.playlist.item

import android.os.Build
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ImageSpan
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper

class ItemModelMapper constructor(
    private val res: ResourceWrapper
) {
    fun mapTopText(model: ItemContract.Model, playing: Boolean): SpannableString {
        if (!playing) {
            return SpannableString(model.title)
        }
        return replaceSpannableIcon(
            "  " + model.title,
            R.drawable.ic_player_play_black,
            R.color.text_primary,
            R.dimen.list_item_top_text_size,
            0,
            2
        )
    }

    fun mapBottomText(model: ItemContract.Model): SpannableString {
        return replaceSpannableIcon(
            model.run { "   $positon  $published / $watched" },
            if (model.starred) R.drawable.ic_button_starred_white else R.drawable.ic_button_unstarred_white,
            R.color.text_secondary,
            R.dimen.list_item_bottom_text_size,
            0,
            2
        )
    }

    private fun replaceSpannableIcon(
        string: String,
        icon: Int,
        @ColorRes tint: Int,
        @DimenRes textSize: Int,
        start: Int,
        end: Int
    ): SpannableString {
        val textPixelSize = res.getDimensionPixelSize(textSize)
        return SpannableString(string).apply {
            setSpan(
                ImageSpan(
                    res.getDrawable(icon, tint).apply { setBounds(start, start, textPixelSize, textPixelSize); },
                    if (Build.VERSION.SDK_INT >= 29) ImageSpan.ALIGN_CENTER else ImageSpan.ALIGN_BASELINE
                ),
                start, end,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )

        }
    }
}