package uk.co.sentinelweb.cuer.app.util.wrapper

import android.content.Context
import androidx.annotation.StringRes

class ResourceWrapper constructor(
    private val context: Context
) {

    fun getString(@StringRes id: Int) = context.getString(id)

    fun getString(@StringRes id: Int, vararg params: Any) = context.resources.getString(id, *params)
}
