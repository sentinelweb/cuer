package uk.co.sentinelweb.cuer.app.ui.common.compose.image

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.layout.ContentScale
import coil.annotation.ExperimentalCoilApi
import coil.compose.ImagePainter
import coil.compose.rememberImagePainter
import uk.co.sentinelweb.cuer.app.R

/**
 * A wrapper around [Image] and [rememberImagePainter], setting a
 * default [contentScale] and showing content while loading.
 */
@OptIn(ExperimentalCoilApi::class)
@Composable
fun NetworkImage(
    url: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    placeholderColor: Color? = MaterialTheme.colors.compositedOnSurface(0.2f),
) {
    Box(modifier) {
        val painter = rememberImagePainter(
            data = url,
            builder = {
                placeholder(drawableResId = R.drawable.ic_image_placeholder)
            }
        )

        Image(
            painter = painter,
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = Modifier.fillMaxSize()
        )

        if (painter.state is ImagePainter.State.Loading && placeholderColor != null) {
            Spacer(
                modifier = Modifier
                    .matchParentSize()
                    .background(placeholderColor)
            )
        }
    }
}

/**
 * Return the fully opaque color that results from compositing [onSurface] atop [surface] with the
 * given [alpha]. Useful for situations where semi-transparent colors are undesirable.
 */
@Composable
fun Colors.compositedOnSurface(alpha: Float): androidx.compose.ui.graphics.Color {
    return onSurface.copy(alpha = alpha).compositeOver(surface)
}