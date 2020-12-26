package uk.co.sentinelweb.cuer.app.util.wrapper

import android.content.Context
import android.content.res.Resources
import androidx.annotation.DimenRes
import androidx.annotation.StringRes

class ResourceWrapper constructor(
    private val context: Context
) {

    val resources: Resources = context.resources

    fun getString(@StringRes id: Int) = context.getString(id)

    fun getString(@StringRes id: Int, vararg params: Any) = context.resources.getString(id, *params)

    fun getDimensionPixelSize(@DimenRes id: Int): Int = context.resources.getDimensionPixelSize(id)
}
