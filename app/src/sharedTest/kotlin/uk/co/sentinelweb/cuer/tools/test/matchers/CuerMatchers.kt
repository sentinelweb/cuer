package uk.co.sentinelweb.cuer.tools.test.matchers

import android.content.Context
import androidx.annotation.DrawableRes

fun drawableMatches(@DrawableRes id: Int, targetContext: Context? = null) =
    DrawableMatcher(targetContext, id)