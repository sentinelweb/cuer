package uk.co.sentinelweb.cuer.app.ui.playlists.item

import android.graphics.drawable.Drawable
import android.text.SpannableString
import androidx.annotation.DrawableRes
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.common.mapper.IconMapper
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper

class ItemModelMapper constructor(
    private val res: ResourceWrapper,
    private val iconMapper: IconMapper
) {

    private val playDrawable: Drawable by lazy {
        res.getDrawable(R.drawable.ic_player_play_black, R.color.text_primary, R.dimen.list_item_top_text_size, SCALING)
    }

    private val starDrawable: Drawable by lazy {
        res.getDrawable(R.drawable.ic_button_starred_white, R.color.text_secondary, R.dimen.list_item_bottom_text_size, SCALING)
    }

    private val unstarDrawable: Drawable by lazy {
        res.getDrawable(R.drawable.ic_button_unstarred_white, R.color.text_secondary, R.dimen.list_item_bottom_text_size, SCALING)
    }

    private val unwatchDrawable: Drawable by lazy {
        res.getDrawable(R.drawable.ic_visibility_off_24, R.color.text_secondary, R.dimen.list_item_bottom_text_size, SCALING)
    }

    private val watchDrawable: Drawable by lazy {
        res.getDrawable(R.drawable.ic_visibility_24, R.color.text_secondary, R.dimen.list_item_bottom_text_size, SCALING)
    }

    private val _bottomDrawables: MutableMap<Int, Drawable> = mutableMapOf()

    private fun bottomDrawable(@DrawableRes id: Int): Drawable {
        return if (_bottomDrawables.containsKey(id)) {
            _bottomDrawables[id] ?: throw IllegalArgumentException("no drawable for $id")
        } else {
            res.getDrawable(id, R.color.text_secondary, R.dimen.list_item_bottom_text_size, SCALING).apply {
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
        val countText = if (model.count > 0) model.run { "$newItems / $count" } else ""
        return SpannableString("          $countText").apply {
            res.replaceSpannableIcon(
                this,
                bottomDrawable(iconMapper.map(model.type, model.platform)),
                0, 1
            )

            res.replaceSpannableIcon(
                this,
                if (model.starred) starDrawable else unstarDrawable,
                2, 3
            )

            res.replaceSpannableIcon(
                this,
                bottomDrawable(iconMapper.map(model.loopMode)),
                5, 6
            )

            res.replaceSpannableIcon(
                this,
                if (model.watched) watchDrawable else unwatchDrawable,
                8, 9
            )
        }
    }

    companion object {

        private val SCALING = 1.1f
    }
}