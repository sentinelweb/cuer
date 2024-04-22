package uk.co.sentinelweb.cuer.app.util.wrapper

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

class AndroidVibrateWarpper(private val context: Context):VibrateWrapper {

    override fun vibrate(time: Long ) {
        val v: Vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(time, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            v.vibrate(time);
        }
    }
}
