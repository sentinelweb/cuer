package uk.co.sentinelweb.cuer.app.ui.ytplayer.floating

import android.app.Service
import android.content.Context
import android.graphics.PixelFormat
import android.graphics.PointF
import android.graphics.Rect
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
import android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
import androidx.annotation.DrawableRes
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.databinding.WindowAytFloatBinding
import uk.co.sentinelweb.cuer.app.util.extension.view.fadeIn
import uk.co.sentinelweb.cuer.app.util.extension.view.fadeOut
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferences.FLOATING_PLAYER_RECT
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferencesWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain
import kotlin.math.max
import kotlin.math.min

class FloatingWindowManagement(
    private val service: Service,
    private val res: ResourceWrapper,
    private val coroutineContextProvider: CoroutineContextProvider,
    private val prefs: MultiPlatformPreferencesWrapper,
    private val log: LogWrapper,
) {
    init {
        log.tag(this)
    }

    interface Callbacks {
        fun onClose()
        fun onLaunch()
        fun onPlayPause()
    }

    private var _binding: WindowAytFloatBinding? = null
    private var _floatWindowLayoutParam: WindowManager.LayoutParams? = null
    private var _windowManager: WindowManager? = null
    lateinit var callbacks: Callbacks

    val binding: WindowAytFloatBinding
        get() = _binding!!

    fun makeWindowWithView() {
        val displayWidth = res.screenWidth
        val displayHeight = res.screenHeight

        _windowManager = service.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val layoutInflater = service.baseContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        _binding = WindowAytFloatBinding.inflate(layoutInflater)

        _floatWindowLayoutParam = loadWindowParams() ?: defaultWindowParams(displayWidth, displayHeight)

        _windowManager?.addView(_binding!!.root, _floatWindowLayoutParam)

        binding.floatingPlayerClose.setOnClickListener {
            callbacks.onClose()
        }
        binding.floatingPlayerLaunch.setOnClickListener {
            callbacks.onLaunch()
        }

        listenMutliTouch()
        updateGestureExclusion()
    }

    private fun listenMutliTouch() {
        binding.floatingPlayerMultiTouch.callbacks = object : MultiTouchView.Callbacks {
            private lateinit var floatWindowLayoutUpdateParam: WindowManager.LayoutParams
            private var initialSize: PointF? = null
            private var initialPosition: PointF? = null
            private var minSize = res.getDimensionPixelSize(R.dimen.min_floating_window_size)
            private var maxWidth = res.screenWidth
            private var maxHeight = res.screenHeight

            override fun onDown() {
                floatWindowLayoutUpdateParam = _floatWindowLayoutParam!!
                initialSize = _floatWindowLayoutParam!!.run { PointF(width.toFloat(), height.toFloat()) }
                initialPosition = _floatWindowLayoutParam!!.run { PointF(x.toFloat(), y.toFloat()) }
                showControls()
            }

            override fun onMove(dx: Float, dy: Float) {
                val newX = (initialPosition!!.x + dx).toInt()
                val diffX = newX - floatWindowLayoutUpdateParam.x
                floatWindowLayoutUpdateParam.x = newX
                val newY = (initialPosition!!.y + dy).toInt()
                val diffY = newY - floatWindowLayoutUpdateParam.y
                floatWindowLayoutUpdateParam.y = newY
                log.d("move: win:[${floatWindowLayoutUpdateParam.x} ${floatWindowLayoutUpdateParam.y}] diff:[$diffX, $diffY] d:[${dx.toInt()} ${dy.toInt()}]")
                _windowManager!!.updateViewLayout(binding.root, floatWindowLayoutUpdateParam)
            }

            override fun onResize(currentScale: PointF) {
                val scaleFactor = 0.70
                floatWindowLayoutUpdateParam.width =
                    min(max((initialSize!!.x * currentScale.x * scaleFactor).toInt(), minSize), maxWidth)
                floatWindowLayoutUpdateParam.height =
                    min(max((initialSize!!.y * currentScale.y * scaleFactor).toInt(), minSize), maxHeight)
                _windowManager!!.updateViewLayout(binding.root, floatWindowLayoutUpdateParam)
            }

            override fun onClick() {
                callbacks.onPlayPause()
            }

            override fun onCommit() {
                initialSize = null
                initialPosition = null
                _floatWindowLayoutParam = floatWindowLayoutUpdateParam
                saveWindowParams()
                updateGestureExclusion()
            }
        }
    }

    private fun showControls() {
        binding.floatingPlayerControls.apply {
            if (!isVisible) {
                coroutineContextProvider.mainScope.launch {
                    delay(2000)
                    fadeOut()
                }
            }
            if (!isVisible) fadeIn() // fadeOut() else
        }
    }

    private fun updateGestureExclusion() {
        if (Build.VERSION.SDK_INT < 29) return
        val rect = _floatWindowLayoutParam!!.run { Rect(x, y, x + width, y + height) }
        ViewCompat.setSystemGestureExclusionRects(binding.root, listOf(rect))
    }

    fun saveWindowParams() {
        _floatWindowLayoutParam?.apply {
            prefs.putString(FLOATING_PLAYER_RECT, "$x:$y:$width:$height")
        }
    }

    fun loadWindowParams() =
        prefs.getString(FLOATING_PLAYER_RECT, null)?.let {
            val split = it.split(":")
            WindowManager.LayoutParams(
                split[2].toInt(),
                split[3].toInt(),
                TYPE_APPLICATION_OVERLAY,
                FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.START or Gravity.TOP
                x = split[0].toInt()
                y = split[1].toInt()
            }
        }

    private fun defaultWindowParams(dWidth: Int, dHeight: Int): WindowManager.LayoutParams {
        val width = (dWidth * 0.55f).toInt()
        val height = (width * 9f / 16).toInt()
        val top = 0
        val left = dWidth - width
        return WindowManager.LayoutParams(
            width, height, TYPE_APPLICATION_OVERLAY, FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.START or Gravity.TOP; x = left; y = top }
    }

    fun cleanup() {
        _windowManager?.removeView(_binding!!.root)
    }

    fun setPlayerState(it: PlayerStateDomain) {
        when (it) {
            PlayerStateDomain.PLAYING -> setPlayIndicator(R.drawable.ic_player_play)
            PlayerStateDomain.PAUSED -> setPlayIndicator(R.drawable.ic_player_pause)
//            PlayerStateDomain.BUFFERING,
//            PlayerStateDomain.UNKNOWN,
//            PlayerStateDomain.VIDEO_CUED -> setPlayIndicator(R.drawable.ic_refresh)

            else -> binding.floatingPlayerPause.isVisible = false
        }
    }

    private fun setPlayIndicator(@DrawableRes drawable: Int) {
        binding.floatingPlayerPause.isVisible = true
        binding.floatingPlayerPause.setImageResource(drawable)
        showControls()
    }
}
