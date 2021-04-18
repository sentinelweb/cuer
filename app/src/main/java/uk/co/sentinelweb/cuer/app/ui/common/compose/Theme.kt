package uk.co.sentinelweb.cuer.app.ui.common.compose

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import com.google.android.material.composethemeadapter.createMdcTheme

// see: https://github.com/material-components/material-components-android-compose-theme-adapter
@Composable
fun CuerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val layoutDirection = LocalLayoutDirection.current
    var (colors, type, shapes) = createMdcTheme(
        context = context,
        layoutDirection = layoutDirection
    )

    MaterialTheme(
        colors = colors!!,
        typography = CuerTypography,
        shapes = makeShapes(),
        content = content
    )
}

