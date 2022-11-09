package uk.co.sentinelweb.cuer.app.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.common.compose.CuerTheme
import uk.co.sentinelweb.cuer.app.ui.common.compose.cuerOutlineButtonColors
import uk.co.sentinelweb.cuer.app.ui.common.compose.cuerOutlineButtonStroke
import uk.co.sentinelweb.cuer.app.ui.resources.ActionResources

object OnboardingComposables {
    var isTransitioning = false
    @Composable
    fun OnboardingUi(view: OnboardingViewModel) {
        OnboardingView(view.model.collectAsState().value, view)
    }

    @Composable
    fun OnboardingView(
        model: OnboardingContract.Model,
        view: OnboardingViewModel
    ) {
        isTransitioning = false
        CuerTheme {
            Surface {
                val scope = rememberCoroutineScope()
                Box(
                    modifier = Modifier
                        .padding(top = 128.dp)
                ) {
                    val states = mutableListOf<MutableTransitionState<Boolean>>()
                    Column(
                        modifier = Modifier
//                            .align(Alignment.TopCenter)
                            .padding(32.dp)
                            .fillMaxWidth()
                    ) {
                        val layoutModifiers = Modifier.align(Alignment.CenterHorizontally)
                        states.add(animatedText(model.screen.title, 0) {
                            Line(model.screen.title, true, layoutModifiers)
                        })
                        model.screen.lines.forEachIndexed { i, line ->
                            states.add(animatedText(line, i + 1) {
                                Line(line, false, layoutModifiers)
                            })
                        }
                    }
                    states.add(animatedText(Any(), states.size) {
                        ButtonsRow(model) { transitionToNext(states, view, scope) }
                    })
                }
            }
        }
    }

    @Composable
    private fun ButtonsRow(
        model: OnboardingContract.Model,
        modifier: Modifier = Modifier, // , Modifier.align(Alignment.BottomCenter) // doesnt work
        onClick: () -> Unit
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(16.dp)
        ) {
            Button(
                modifier = Modifier
                    .padding(bottom = 168.dp, end = 32.dp) // todo position when finished
                    .align(Alignment.BottomEnd),
                border = cuerOutlineButtonStroke(),
                colors = cuerOutlineButtonColors(),
                onClick = onClick
            ) {
                Text(stringResource(R.string.next))
            }

            Text(
                model.screenPosition, modifier = Modifier
                    .padding(bottom = 180.dp)
                    .align(Alignment.BottomCenter)
            )

            Button(
                modifier = Modifier
                    .padding(bottom = 168.dp, start = 32.dp) // todo position when finished
                    .align(Alignment.BottomStart),
                colors = cuerOutlineButtonColors(),
                onClick = {}
            ) {
                Text(stringResource(R.string.skip))
            }
        }
    }

    private fun transitionToNext(
        states: List<MutableTransitionState<Boolean>>,
        view: OnboardingViewModel,
        scope: CoroutineScope
    ) {
        if (!isTransitioning) {
            scope.launch {
                states.reversed().forEach {
                    it.targetState = false
                    delay(300)
                }
                view.onNext()
            }
            isTransitioning = true
        }

    }

    @Composable
    private fun animatedText(
        line: Any,
        seq: Int,
        block: @Composable () -> Unit
    ): MutableTransitionState<Boolean> {
        val state = remember(line) {
            MutableTransitionState(false)
        }
        LaunchedEffect(line) {
            delay(seq * 300L)
            state.targetState = true
        }
        AnimatedVisibility(
            state,
            enter = fadeIn(animationSpec = tween(durationMillis = 1000)),
            exit = fadeOut(animationSpec = tween(durationMillis = 1000))
        ) {
            block()
        }
        return state
    }

    @Composable
    private fun Line(
        line: ActionResources,
        isTitle: Boolean,
        modifier: Modifier = Modifier
    ) {
        Row(
            modifier = modifier
                //                    .background(Color.Red)
                .padding(16.dp)
        ) {
            val color = line.color?.let { colorResource(it) } ?: MaterialTheme.colors.onSurface
            line.icon?.also {
                Icon(
                    painter = painterResource(it),
                    tint = color,
                    contentDescription = stringResource(id = R.string.menu_search),
                    modifier = Modifier
                        //                            .background(Color.Green)
                        .padding(end = 8.dp)
                        .size(24.dp)
                        .align(Alignment.CenterVertically)
                )
            }
            line.label?.also {
                if (isTitle) {
                    TitleText(color, it)
                } else {
                    LineText(color, it)
                }
            }
        }
    }

    @Composable
    private fun LineText(color: Color, it: String, modifier: Modifier = Modifier) {
        Text(
            modifier = modifier,
            style = MaterialTheme.typography.body1,
            color = color,
            text = it
        )
    }

    @Composable
    private fun TitleText(color: Color, it: String, modifier: Modifier = Modifier) {
        Text(
            modifier = modifier,
            style = MaterialTheme.typography.h3,
            color = color,
            text = it
        )
    }
}
