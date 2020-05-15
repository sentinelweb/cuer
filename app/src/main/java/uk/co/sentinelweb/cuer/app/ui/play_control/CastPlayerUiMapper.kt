package uk.co.sentinelweb.cuer.app.ui.play_control

import java.text.SimpleDateFormat
import java.util.*

class CastPlayerUiMapper {
    private var sdf: SimpleDateFormat = SimpleDateFormat("mm:ss", Locale.getDefault())
        .apply { timeZone = TimeZone.getTimeZone("GMT") }

    fun formatTime(ms: Long): String {
        return sdf.format(Date(ms))
    }
}