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
        //val metrics: DisplayMetrics = service.getApplicationContext().getResources().getDisplayMetrics()
        val displayWidth = res.screenWidth //metrics.widthPixels
        val displayHeight = res.screenHeight //metrics.heightPixels

        _windowManager = service.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val layoutInflater = service.baseContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        _binding = WindowAytFloatBinding.inflate(layoutInflater)

        _floatWindowLayoutParam = /*loadWindowParams() ?:*/ defaultWindowParams(displayWidth, displayHeight)

        _windowManager?.addView(_binding!!.root, _floatWindowLayoutParam)

//        addMoveTouchListener()
//        addResizeTouchListener()
        binding.floatingPlayerClose.setOnClickListener {
            callbacks.onClose()
        }
        binding.floatingPlayerLaunch.setOnClickListener {
            callbacks.onLaunch()
        }

        //binding?.floatingPlayerBlockTitle?.setOnClickListener { callbacks.onPlayPause() }
        listenMutliTouch()
//        binding.fullscreenVideoWrapper.listener = object : InterceptorFrameLayout.OnTouchInterceptListener {
//            override fun touched() {
//                binding.floatingPlayerControls.apply {
//                    if (!isVisible) {
//                        coroutineContextProvider.mainScope.launch {
//                            delay(2000)
//                            fadeOut()
//                        }
//                    }
//                    if (isVisible) fadeOut() else fadeIn()
//                }
//            }
//        }
        updateGestureExclusion()
    }

    // listen for multitouch view
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
                showControls() // todo check this works on device
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

            override fun onResize(currentScale: Float) {
                floatWindowLayoutUpdateParam.width =
                    min(max((initialSize!!.x * currentScale).toInt(), minSize), maxWidth)
                floatWindowLayoutUpdateParam.height =
                    min(max((initialSize!!.y * currentScale).toInt(), minSize), maxHeight)
                _windowManager!!.updateViewLayout(binding.root, floatWindowLayoutUpdateParam)
            }

            override fun onClick() {
                // fixme this is not working
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

//    private fun addMoveTouchListener() {
//        binding?.floatingPlayerMove?.setOnTouchListener(object : OnTouchListener {
//            val floatWindowLayoutUpdateParam = _floatWindowLayoutParam!!
//            var x = 0.0
//            var y = 0.0
//            var sx = 0.0
//            var sy = 0.0
//            override fun onTouch(v: View, event: MotionEvent): Boolean {
//                when (event.action) {
//                    MotionEvent.ACTION_DOWN -> {
//                        x = floatWindowLayoutUpdateParam.x.toDouble()
//                        y = floatWindowLayoutUpdateParam.y.toDouble()
//                        sx = event.rawX.toDouble()
//                        sy = event.rawY.toDouble()
//                    }
//                    MotionEvent.ACTION_MOVE -> {
//                        floatWindowLayoutUpdateParam.x = (x + event.rawX - sx).toInt()
//                        floatWindowLayoutUpdateParam.y = (y + event.rawY - sy).toInt()
//
//                        _windowManager!!.updateViewLayout(_binding?.root, floatWindowLayoutUpdateParam)
//                    }
//                    MotionEvent.ACTION_UP -> {
//                        _floatWindowLayoutParam = floatWindowLayoutUpdateParam
//                        saveWindowParams()
//                        updateGestureExclusion()
//                    }
//                }
//                return true
//            }
//        })
//    }
//
//    private fun addResizeTouchListener() {
//        _binding?.floatingPlayerResize?.setOnTouchListener(object : OnTouchListener {
//            val floatWindowLayoutUpdateParam = _floatWindowLayoutParam!!
//            var w = 0.0
//            var h = 0.0
//            var sx = 0.0
//            var sy = 0.0
//            var minSize = res.getDimensionPixelSize(R.dimen.min_floating_window_size).toDouble()
//            override fun onTouch(v: View, event: MotionEvent): Boolean {
//                when (event.action) {
//                    MotionEvent.ACTION_DOWN -> {
//                        w = floatWindowLayoutUpdateParam.width.toDouble()
//                        h = floatWindowLayoutUpdateParam.height.toDouble()
//                        sx = event.rawX.toDouble()
//                        sy = event.rawY.toDouble()
//                    }
//                    MotionEvent.ACTION_MOVE -> {
//                        floatWindowLayoutUpdateParam.width = max(w + (event.rawX - sx), minSize).toInt()
//                        floatWindowLayoutUpdateParam.height = max(h - (event.rawY - sy), minSize).toInt()
//
//                        _windowManager!!.updateViewLayout(_binding?.root, floatWindowLayoutUpdateParam)
//                    }
//                    MotionEvent.ACTION_UP -> {
//                        _floatWindowLayoutParam = floatWindowLayoutUpdateParam
//                        saveWindowParams()
//                        updateGestureExclusion()
//                    }
//                }
//                return true
//            }
//        })
//    }

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
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.START or Gravity.TOP
                x = split[0].toInt()
                y = split[1].toInt()
            }
        }

    private fun defaultWindowParams(dWidth: Int, dHeight: Int): WindowManager.LayoutParams {
        val width = (dWidth * 0.55f).toInt()
        val height = (width * 3f / 4).toInt()
        val top = 0
        val left = dWidth - width
        return WindowManager.LayoutParams(
            width,
            height,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.START or Gravity.TOP
            x = left
            y = top
        }
    }

    fun cleanup() {
        _windowManager?.removeView(_binding!!.root)
    }

}