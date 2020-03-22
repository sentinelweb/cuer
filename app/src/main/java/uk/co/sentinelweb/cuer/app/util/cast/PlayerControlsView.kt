package uk.co.sentinelweb.cuer.app.util.cast

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import uk.co.sentinelweb.cuer.app.R

class PlayerControlsView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    FrameLayout(context, attrs, defStyleAttr) {

    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null, 0)

    init {
        LayoutInflater.from(context).inflate(R.layout.view_player_controls_example, this)
    }
}