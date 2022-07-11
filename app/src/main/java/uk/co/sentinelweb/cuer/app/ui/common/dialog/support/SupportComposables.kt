package uk.co.sentinelweb.cuer.app.ui.common.dialog.support

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.mvikotlin.core.view.BaseMviView
import uk.co.sentinelweb.cuer.app.ui.common.compose.CuerTheme
import uk.co.sentinelweb.cuer.app.ui.support.SupportContract.View.Event
import uk.co.sentinelweb.cuer.app.ui.support.SupportContract.View.Model

object SupportComposables {

    @Composable
    fun SupportUi(view: SupportMviView) {
        SupportView(view.observableModel, view)
    }

    @Composable
    fun SupportView(model: Model, view: BaseMviView<Model, Event>) {
        CuerTheme {
            Surface {
                Column {
                    model.links.forEach {
                        Row(
                            modifier = Modifier
                                .clickable(onClick = { view.dispatch(Event.OnLinkClicked(it)) })
                        ) {
                            Text(
                                text = it.title,
                                style = MaterialTheme.typography.body1,
                                modifier = Modifier.padding(
                                    start = 16.dp,
                                    top = 16.dp,
                                    end = 16.dp,
                                    bottom = 8.dp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}