package uk.co.sentinelweb.cuer.app.ui.common.compose

import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.dimensionResource
import uk.co.sentinelweb.cuer.app.R

@Composable
fun makeShapes() = Shapes(
    small = CutCornerShape(topStart = dimensionResource(R.dimen.small_corner_cut), bottomEnd = dimensionResource(R.dimen.small_corner_cut)),
    medium = CutCornerShape(
        topStart = dimensionResource(R.dimen.medium_corner_cut),
        bottomEnd = dimensionResource(R.dimen.medium_corner_cut)
    ),
    large = CutCornerShape(topStart = dimensionResource(R.dimen.large_corner_cut), bottomEnd = dimensionResource(R.dimen.large_corner_cut)),
)
