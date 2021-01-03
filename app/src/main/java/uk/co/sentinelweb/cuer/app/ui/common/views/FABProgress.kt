package uk.co.sentinelweb.cuer.app.ui.common.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Job
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider


class FABProgress(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    FloatingActionButton(context, attrs, defStyleAttr), KoinComponent {

    constructor(context: Context, attrs: AttributeSet?) :
            this(context, attrs, R.attr.floatingActionButtonStyle)

    constructor(context: Context) :
            this(context, null, R.attr.floatingActionButtonStyle)

    private val res: ResourceWrapper by inject()
    private val contextProvider: CoroutineContextProvider by inject()
    private var showProgress = false
    private var progressMargin: Int = 0
    private val arcPaint = Paint().apply {
        color = res.getColor(R.color.player_button_progress)
        strokeWidth = res.getDimensionPixelSize(4f).toFloat()
        style = Paint.Style.STROKE
    }

    private var tickerChannel: ReceiveChannel<Unit>? = null
    private var receiveJob: Job? = null

    init {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.FABProgress, 0, 0)
        try {
            arcPaint.color = a.getColor(R.styleable.FABProgress_progressColor, res.getColor(R.color.player_button_progress))
            progressMargin = a.getDimensionPixelSize(R.styleable.FABProgress_progressMargin, 0)
        } finally {
            a.recycle()
        }
    }

    private object animVars {
        var angle = 0f
        var sweep = 270f
        var sweepInc = 2f

        fun inc() {
            angle += 5
            sweep += sweepInc
            if (sweep == 320f) sweepInc = -2f
            else if (sweep == 90f) sweepInc = 2f
        }
    }

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)

        if (showProgress) {
            canvas?.drawArc(
                left - x + progressMargin,
                top.toFloat() - y + progressMargin,
                right - x - progressMargin,
                bottom.toFloat() - y - progressMargin,
                animVars.angle,
                animVars.sweep,
                false,
                arcPaint
            )
        }
    }

    @ObsoleteCoroutinesApi // fixme replace ticker with something else
    fun showProgress(show: Boolean) {
        showProgress = show
        if (show && tickerChannel == null) {
            tickerChannel = ticker(delayMillis = 1000 / 60, initialDelayMillis = 0)
            receiveJob = contextProvider.MainScope.launch {
                tickerChannel?.apply {
                    for (event in this) {
                        animVars.inc()
                        invalidate()
                    }
                }
            }
        } else if (!show) {
            tickerChannel?.cancel()
            receiveJob?.cancel()
            tickerChannel = null
        }
    }

}