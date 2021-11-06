package uk.co.sentinelweb.cuer.app.ui.playlists.item

import android.graphics.drawable.Drawable
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.text.toSpannable
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.common.mapper.IconMapper
import uk.co.sentinelweb.cuer.app.ui.playlists.item.ItemContract.ItemType.ROW
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper
// todo ditch this and just add the icons to the views
class ItemModelMapper constructor(
    private val res: ResourceWrapper,
    private val iconMapper: IconMapper
) {

    private fun playDrawable(@ColorRes color:Int = R.color.text_primary): Drawable =
        res.getDrawable(R.drawable.ic_player_play_black, color, R.dimen.list_item_top_text_size, SCALING)

    private fun starDrawable(@ColorRes color:Int = R.color.text_primary): Drawable =
        res.getDrawable(R.drawable.ic_button_starred_white, color, R.dimen.list_item_bottom_text_size, SCALING)


    private fun unstarDrawable(@ColorRes color:Int = R.color.text_primary): Drawable =
        res.getDrawable(R.drawable.ic_button_unstarred_white, color, R.dimen.list_item_bottom_text_size, SCALING)

    private val unwatchDrawable: Drawable by lazy {
        res.getDrawable(R.drawable.ic_visibility_off_24, R.color.text_secondary, R.dimen.list_item_bottom_text_size, SCALING)
    }

    private val watchDrawable: Drawable by lazy {
        res.getDrawable(R.drawable.ic_visibility_24, R.color.text_secondary, R.dimen.list_item_bottom_text_size, SCALING)
    }

    private fun pinDrawable(@ColorRes color:Int = R.color.text_primary): Drawable =
        res.getDrawable(R.drawable.ic_push_pin_on_24, color, R.dimen.list_item_bottom_text_size, SCALING)

    private fun defaultDrawable(@ColorRes color:Int = R.color.text_primary): Drawable =
        res.getDrawable(R.drawable.ic_playlist_default_black,color, R.dimen.list_item_bottom_text_size, SCALING)

    private val tree: Drawable by lazy {
        res.getDrawable(R.drawable.ic_tree_24, R.color.text_secondary, R.dimen.list_item_bottom_text_size, SCALING)
    }

    private fun textColor(type:ItemContract.ItemType) = if (type==ItemContract.ItemType.TILE) {
        R.color.white
    } else {
        R.color.text_primary
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

    fun mapTopText(model: ItemContract.Model.ItemModel, playing: Boolean, type:ItemContract.ItemType): Spannable {
        val builder = SpannableStringBuilder(model.title)
        if (type == ItemContract.ItemType.TILE) {
            if (model.pinned) {
                addIconToStart(builder, pinDrawable(textColor(type)))
            }
            if (model.default) {
                addIconToStart(builder, defaultDrawable(textColor(type)))
            }
        }
        if (playing) {
            addIconToStart(builder, playDrawable(textColor(type)))
        }
        return builder.toSpannable()
    }

    private fun addIconToStart(builder: SpannableStringBuilder, drawable: Drawable) {
        val str = SpannableString("  ")
        res.replaceSpannableIcon(str, drawable, 0, 1)
        builder.insert(0, str)
    }

    fun mapBottomText(model: ItemContract.Model.ItemModel): Spannable {
        val countText = if (model.count > 0) model.run { "$newItems / $count " } else ""
        val base = SpannableString("          $countText").let {
            res.replaceSpannableIcon(
                it,
                bottomDrawable(iconMapper.map(model.type, model.platform)),
                0, 1
            )

            res.replaceSpannableIcon(
                it,
                if (model.starred) starDrawable(textColor(ROW)) else unstarDrawable(textColor(ROW)),
                2, 3
            )

            res.replaceSpannableIcon(
                it,
                bottomDrawable(iconMapper.map(model.loopMode)),
                5, 6
            )

            res.replaceSpannableIcon(
                it,
                if (model.watched) watchDrawable else unwatchDrawable,
                8, 9
            )
            it
        }
        val builder = SpannableStringBuilder()
        builder.append(base)
        if (model.pinned) {
            val str = SpannableString("  ")
            res.replaceSpannableIcon(
                str,
                pinDrawable(textColor(ROW)),
                0, 1
            )
            builder.append(str)
        }

        if (model.default) {
            val str = SpannableString("  ")
            res.replaceSpannableIcon(
                str,
                defaultDrawable(textColor(ROW)),
                0, 1
            )
            builder.append(str)
        }
        if (model.descendents > 0) {
            val str = SpannableString("  ${model.descendents}")
            res.replaceSpannableIcon(
                str,
                tree,
                0, 1
            )
            builder.append(str)
        }
        return builder.toSpannable()
    }

    companion object {
        private val SCALING = 1.1f
    }
}