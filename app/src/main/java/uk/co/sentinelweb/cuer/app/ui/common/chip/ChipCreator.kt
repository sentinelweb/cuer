package uk.co.sentinelweb.cuer.app.ui.common.chip

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.google.android.material.chip.Chip
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel.Type.PLAYLIST_SELECT

class ChipCreator(private val c: Context) {

    fun create(model: ChipModel, parent: ViewGroup): Chip? = when (model.type) {
        PLAYLIST_SELECT -> {
            (LayoutInflater.from(c)
                .inflate(R.layout.playlist_chip_select, parent, false) as Chip).apply {
                tag = model
            }
        }
        else -> null
    }
}