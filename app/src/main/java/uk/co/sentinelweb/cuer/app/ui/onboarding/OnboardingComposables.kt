package uk.co.sentinelweb.cuer.app.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.common.compose.CuerTheme
import uk.co.sentinelweb.cuer.app.ui.common.compose.cuerOutlineButtonColors
import uk.co.sentinelweb.cuer.app.ui.common.compose.cuerOutlineButtonStroke
import uk.co.sentinelweb.cuer.app.ui.resources.ActionResources

object OnboardingComposables {
    @Composable
    fun OnboardingUi(view: OnboardingViewModel) {
        OnboardingView(view.model.collectAsState().value, view)
    }

    @Composable
    fun OnboardingView(
        model: OnboardingContract.Model,
        view: OnboardingViewModel
    ) {
        CuerTheme {
            Surface {
                Box(
                    modifier = Modifier
                        .padding(top = 128.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                    ) {
                        Title(model.screen.title, modifier = Modifier.align(Alignment.CenterHorizontally))
                        model.screen.lines.forEach { line ->
                            Line(line, modifier = Modifier.align(Alignment.CenterHorizontally))
                        }
                    }
                    Button(
                        modifier = Modifier
                            .padding(bottom = 168.dp, end = 16.dp) // todo position when finished
                            .align(Alignment.BottomEnd),
                        border = cuerOutlineButtonStroke(),
                        colors = cuerOutlineButtonColors(),
                        onClick = { view.onNext() }) {
                        Text("Next")
                    }
                }
            }
        }
    }

    @Composable
    private fun Line(line: ActionResources, modifier: Modifier) {
        val state = remember {
            MutableTransitionState(false).apply {
                // Start the animation immediately.
                targetState = true
            }
        }
        AnimatedVisibility(
            state,
            enter = fadeIn(animationSpec = tween(durationMillis = 1000)),
            exit = fadeOut(animationSpec = tween(durationMillis = 1000))
        ) {
            Row(
                modifier = modifier
                    .padding(16.dp)
            ) {
                val color = line.color?.let { colorResource(it) } ?: MaterialTheme.colors.onSurface
                line.icon?.also {
                    Icon(
                        painter = painterResource(it),
                        tint = color,
                        contentDescription = stringResource(id = R.string.menu_search),
                        modifier = Modifier.padding(end = 8.dp).size(24.dp)
                    )
                }
                line.label?.also {
                    Text(
                        modifier = Modifier,
                        style = MaterialTheme.typography.body1,
                        color = color,
                        text = it
                    )
                }
            }
        }
    }

    @Composable
    private fun Title(line: ActionResources, modifier: Modifier) {
        Row(
            modifier = modifier
                .padding(16.dp)
        ) {
            val color = line.color?.let { colorResource(it) } ?: MaterialTheme.colors.onSurface
            line.icon?.also {
                Icon(
                    painter = painterResource(it),
                    tint = color,
                    contentDescription = stringResource(id = R.string.menu_search),
                    modifier = Modifier.padding(end = 8.dp).size(24.dp).align(Alignment.CenterVertically)
                )
            }
            line.label?.also {
                Text(
                    modifier = Modifier,
                    style = MaterialTheme.typography.h3,
                    color = color,
                    text = it
                )
            }
        }
    }
}