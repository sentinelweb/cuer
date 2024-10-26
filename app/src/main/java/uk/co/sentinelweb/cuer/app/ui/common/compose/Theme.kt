package uk.co.sentinelweb.cuer.app.ui.common.compose

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.colorResource
import com.google.android.material.composethemeadapter.createMdcTheme
import uk.co.sentinelweb.cuer.app.R

// see: https://github.com/material-components/material-components-android-compose-theme-adapter
@Composable
fun CuerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val layoutDirection = LocalLayoutDirection.current
    val (colors, type, shapes) = createMdcTheme(
        context = context,
        layoutDirection = layoutDirection
    )

    MaterialTheme(
        colors = colors ?: throw IllegalArgumentException("Colors were not imported from MDC theme"),
        typography = CuerTypography,
        shapes = shapes ?: throw IllegalArgumentException("Shapes were not imported from MDC theme"),
        content = content
    )
}

@Composable
fun CuerBrowseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val layoutDirection = LocalLayoutDirection.current
    var (colors, type, shapes) = createMdcTheme(
        context = context,
        layoutDirection = layoutDirection
    )
    colors = colors?.copy(
        onSurface = Color.White,
        surface = colorResource(id = R.color.indigo_400),
        primary = colorResource(id = R.color.secondary),
        secondary = colorResource(id = R.color.secondary),
    )
    MaterialTheme(
        colors = colors ?: throw IllegalArgumentException("Colors were not imported from MDC theme"),
        typography = CuerTypography,
        shapes = shapes ?: throw IllegalArgumentException("Shapes were not imported from MDC theme"),
        content = content
    )
}



