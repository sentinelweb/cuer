package uk.co.sentinelweb.cuer.app.ui.ytplayer.floating

import android.app.Service
import android.content.Context
import android.graphics.PixelFormat
import android.util.DisplayMetrics
import android.view.*
import android.view.View.OnTouchListener
import uk.co.sentinelweb.cuer.app.databinding.WindowAytFloatBinding
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper

class FloatingWindowManagement(
    private val service: Service,
    private val res: ResourceWrapper,
) {
    private var _binding: WindowAytFloatBinding? = null
    private var _floatWindowLayoutParam: WindowManager.LayoutParams? = null
    private var _windowManager: WindowManager? = null

    val binding: WindowAytFloatBinding?
        get() = _binding

    fun makeWindowWithView() {
        // The screen height and width are calculated, cause
        // the height and width of the floating window is set depending on this
        val metrics: DisplayMetrics = service.getApplicationContext().getResources().getDisplayMetrics()
        val displayWidth = metrics.widthPixels
        val displayHeight = metrics.heightPixels

        //To obtain a WindowManager of a different Display,
        //we need a Context for that display, so WINDOW_SERVICE is used
        _windowManager = service.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val layoutInflater = service.baseContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        _binding = WindowAytFloatBinding.inflate(layoutInflater)

        // Now the Parameter of the floating-window layout is set.
        // 1) The Width of the window will be 55% of the phone width.
        // 2) The Height of the window will be 58% of the phone height.
        // 3) Layout_Type is already set.
        // 4) Next Parameter is Window_Flag. Here FLAG_NOT_FOCUSABLE is used. But
        // problem with this flag is key inputs can't be given to the EditText.
        // This problem is solved later.
        // 5) Next parameter is Layout_Format. System chooses a format that supports translucency by PixelFormat.TRANSLUCENT
        val width = (displayWidth * 0.55f).toInt()
        val height = (width * 3f / 4).toInt()
        _floatWindowLayoutParam = WindowManager.LayoutParams(
            width,
            height,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
            //X and Y value of the window is set
            x = 0
            y = 0
        }

        // The ViewGroup that inflates the floating_layout.xml is
        // added to the WindowManager with all the parameters
        _windowManager?.addView(_binding!!.root, _floatWindowLayoutParam)

        addTouchListener()
    }

    private fun addTouchListener() {
        _binding?.playerContainer?.setOnTouchListener(object : OnTouchListener {
            val floatWindowLayoutUpdateParam = _floatWindowLayoutParam!!
            var x = 0.0
            var y = 0.0
            var px = 0.0
            var py = 0.0
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        x = floatWindowLayoutUpdateParam.x.toDouble()
                        y = floatWindowLayoutUpdateParam.y.toDouble()
                        px = event.rawX.toDouble()
                        py = event.rawY.toDouble()
                    }
                    MotionEvent.ACTION_MOVE -> {
                        floatWindowLayoutUpdateParam.x = (x + event.rawX - px).toInt()
                        floatWindowLayoutUpdateParam.y = (y + event.rawY - py).toInt()

                        _windowManager!!.updateViewLayout(_binding?.root, floatWindowLayoutUpdateParam)
                    }
                }
                return true
            }
        })
    }

    fun cleanup() {
        _windowManager?.removeView(_binding!!.root)
    }
}