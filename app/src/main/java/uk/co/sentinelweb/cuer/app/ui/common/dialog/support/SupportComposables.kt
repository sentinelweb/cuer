package uk.co.sentinelweb.cuer.app.ui.common.dialog.support

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.arkivanov.mvikotlin.core.view.BaseMviView
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.common.compose.CuerTheme
import uk.co.sentinelweb.cuer.app.ui.common.mapper.IconMapper
import uk.co.sentinelweb.cuer.app.ui.support.SupportContract.View.Companion.CATEGORY_ORDER
import uk.co.sentinelweb.cuer.app.ui.support.SupportContract.View.Event
import uk.co.sentinelweb.cuer.app.ui.support.SupportContract.View.Model
import uk.co.sentinelweb.cuer.domain.LinkDomain

object SupportComposables : KoinComponent {

    private val iconMapper: IconMapper by inject()

    @Composable
    fun SupportUi(view: SupportMviView) {
        SupportView(view.observableModel, view)
    }

    @Composable
    fun SupportView(model: Model, view: BaseMviView<Model, Event>) {
        CuerTheme {
            Surface {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                ) {
                    if (!model.isInitialised) {
                        Initial()
                    } else if ((model.links?.size ?: 0) > 0) {
                        model.title?.also { Title(title = it) }
                        CATEGORY_ORDER.forEach { cat ->
                            model.links
                                ?.get(cat)
                                ?.let { linkList ->
                                    Category(cat)
                                    linkList.forEach {
                                        Link(it, view)
                                    }
                                }
                        }
                    } else {
                        Empty()
                    }
                }
            }
        }
    }

    @Composable
    private fun Title(title: String) {
        Row(
            modifier = Modifier
                .background(MaterialTheme.colors.primary)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_support),
                contentDescription = null,
                tint = MaterialTheme.colors.onPrimary,
                modifier = Modifier
                    .padding(start = 8.dp, top = 16.dp)
                    .size(24.dp)
                    .alignByBaseline()
            )
            Text(
                text = title,
                style = MaterialTheme.typography.h5,
                color = MaterialTheme.colors.onPrimary,
                modifier = Modifier
                    .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 16.dp)
                    .fillMaxWidth()
            )
        }
    }

    @Composable
    private fun Category(cat: LinkDomain.Category) {
        Row(
            modifier = Modifier
                .background(MaterialTheme.colors.primaryVariant)
        ) {
            Icon(
                painter = painterResource(iconMapper.map(cat)),
                contentDescription = null,
                tint = MaterialTheme.colors.onPrimary,
                modifier = Modifier
                    .padding(start = 8.dp, top = 16.dp)
                    .size(24.dp)
            )
            Text(
                text = cat.toString(),
                style = MaterialTheme.typography.body1,
                color = MaterialTheme.colors.onPrimary,
                modifier = Modifier
                    .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 16.dp)
                    .fillMaxWidth()
            )
        }
    }

    @Composable
    private fun Link(
        it: Model.Link,
        view: BaseMviView<Model, Event>
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = {
                    Log.d("click", it.toString())
                    view.dispatch(Event.OnLinkClicked(it))
                })
        ) {
            Icon(
                painter = painterResource(iconMapper.map(it.domain)),
                contentDescription = null,
                modifier = Modifier
                    .padding(start = 8.dp, top = 20.dp)
                    .size(24.dp)
                    .alignByBaseline()
            )
            Text(
                text = it.title,
                style = MaterialTheme.typography.body1,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 8.dp)
            )
        }
    }

    @Composable
    private fun Initial() {
        Text( // todo move to Strings
            text = stringResource(id = R.string.support_links_loading),
            style = MaterialTheme.typography.body1,
            color = MaterialTheme.colors.onSurface,
            modifier = Modifier
                .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 16.dp)
                .fillMaxWidth()
        )
    }

    @Composable
    private fun Empty() {
        Text( // todo move to Strings
            text = stringResource(id = R.string.support_no_links),
            style = MaterialTheme.typography.body1,
            color = MaterialTheme.colors.onSurface,
            modifier = Modifier
                .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 16.dp)
                .fillMaxWidth()
        )
    }
}