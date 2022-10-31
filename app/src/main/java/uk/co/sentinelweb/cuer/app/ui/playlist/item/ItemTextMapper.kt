package uk.co.sentinelweb.cuer.app.ui.playlist.item

import android.graphics.drawable.Drawable
import android.os.Build
import android.text.SpannableString
import android.text.style.ImageSpan
import androidx.annotation.DrawableRes
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.common.mapper.IconMapper
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper
import uk.co.sentinelweb.cuer.domain.PlatformDomain

class ItemTextMapper constructor(
    private val res: ResourceWrapper,
    private val iconMapper: IconMapper
) {

    private val playDrawable: Drawable by lazy {
        res.getDrawable(
            R.drawable.ic_player_play_black,
            R.color.text_primary,
            R.dimen.list_item_top_text_size,
            SCALING
        )
    }

    private val starDrawable: Drawable by lazy {
        res.getDrawable(
            R.drawable.ic_starred,
            R.color.text_secondary,
            R.dimen.list_item_bottom_text_size,
            SCALING
        )
    }

    private val unstarDrawable: Drawable by lazy {
        res.getDrawable(
            R.drawable.ic_starred_off,
            R.color.text_secondary,
            R.dimen.list_item_bottom_text_size,
            SCALING
        )
    }

    private val unwatchDrawable: Drawable by lazy {
        res.getDrawable(
            R.drawable.ic_visibility_off_24,
            R.color.text_secondary,
            R.dimen.list_item_bottom_text_size,
            SCALING
        )
    }

    private val watchDrawable: Drawable by lazy {
        res.getDrawable(
            R.drawable.ic_visibility_24,
            R.color.text_secondary,
            R.dimen.list_item_bottom_text_size,
            SCALING
        )
    }

    private val _bottomDrawables: MutableMap<Int, Drawable> = mutableMapOf()
    private fun bottomDrawable(@DrawableRes id: Int): Drawable {
        return if (_bottomDrawables.containsKey(id)) {
            _bottomDrawables[id] ?: throw IllegalArgumentException("no drawable for $id")
        } else {
            res.getDrawable(id, R.color.text_secondary, R.dimen.list_item_bottom_text_size, SCALING)
                .apply {
                    _bottomDrawables.set(id, this)
                }
        }
    }

    fun mapTopText(model: ItemContract.Model, playing: Boolean): SpannableString {
        return if (!playing) {
            SpannableString(model.title)
        } else {
            SpannableString("  " + model.title).apply {
                res.replaceSpannableIcon(
                    this,
                    playDrawable,
                    0, 2
                )
            }
        }
    }

    fun mapBottomText(model: ItemContract.Model): SpannableString {
        val posText = model.positon
        val watchedText = model.watchedSince
        val publishedText = model.published
        val playlistText = model.playlistName
        return buildBottomText(
            posText,
            watchedText,
            publishedText,
            playlistText,
            model.platform,
            model.isStarred,
            model.isWatched
        )
    }

    fun buildBottomText(
        posText: String,
        watchedText: String,
        publishedText: String,
        playlistText: String? = null,
        platformDomain: PlatformDomain,
        isStarred: Boolean,
        isWatched: Boolean,
        align: Int = if (Build.VERSION.SDK_INT >= 29) ImageSpan.ALIGN_CENTER else ImageSpan.ALIGN_BOTTOM,
    ): SpannableString {
        return SpannableString("  $posText   $WATCH $watchedText   $PLAT $publishedText  ${playlistText ?: ""}").apply {
            res.replaceSpannableIcon(
                this,
                if (isStarred) starDrawable else unstarDrawable,
                0, 1, align
            )
            val platPos = indexOf(PLAT)
            res.replaceSpannableIcon(
                this,
                bottomDrawable(iconMapper.map(platformDomain)),
                platPos, platPos + PLAT.length, align
            )
            val watchPos = indexOf(WATCH)
            res.replaceSpannableIcon(
                this,
                if (isWatched) watchDrawable else unwatchDrawable,
                watchPos, watchPos + WATCH.length, align
            )
        }
    }

    companion object {
        private val PLAT = "[plat]"
        private val WATCH = "[watch]"
        private val SCALING = 1.1f
    }
}