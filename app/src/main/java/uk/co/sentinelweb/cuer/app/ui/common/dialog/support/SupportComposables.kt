package uk.co.sentinelweb.cuer.app.ui.common.dialog.support

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import uk.co.sentinelweb.cuer.app.ui.common.compose.CuerBrowseTheme
import uk.co.sentinelweb.cuer.app.ui.support.SupportContract

object SupportComposables {

    @Composable
    fun SupportUi(view: SupportMviView) {
        SupportView(view.observableModel)
    }

    @Composable
    fun SupportView(model: SupportContract.View.Model) {
        CuerBrowseTheme {
            Surface {
                Column {

                }
            }
        }
    }
}