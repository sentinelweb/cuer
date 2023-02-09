package uk.co.sentinelweb.cuer.app.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.glide.rememberGlidePainter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.common.compose.CuerTheme
import uk.co.sentinelweb.cuer.app.ui.common.compose.cuerOutlineButtonColors
import uk.co.sentinelweb.cuer.app.ui.common.compose.cuerOutlineButtonStroke
import uk.co.sentinelweb.cuer.app.ui.common.resources.ActionResources

object OnboardingComposables {
    var isTransitioning = false

    @Composable
    fun OnboardingUi(view: OnboardingViewModel) {
        OnboardingView(view.model.collectAsState().value, view)
    }

    @Composable
    fun OnboardingView(
        model: OnboardingContract.Model,
        interactions: OnboardingContract.Interactions
    ) {
        isTransitioning = false
        CuerTheme {
            Surface {
                val scope = rememberCoroutineScope()

                Box(
                    modifier = Modifier
                        .height(512.dp)
                        .background(color = colorResource(id = model.screen.backgroundColor ?: R.color.surface))
                ) {
                    model.screen.backgroundUrl?.also {
                        Image(
                            painter = rememberGlidePainter(
                                request = it,
                                fadeIn = true
                            ),
                            colorFilter = ColorFilter.tint(Color(0xaa000000), blendMode = BlendMode.Multiply),
                            contentDescription = "",
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                                .wrapContentHeight(),
                            contentScale = ContentScale.Crop
                        )

                    }
                    val states = mutableListOf<MutableTransitionState<Boolean>>()
                    Column(
                        modifier = Modifier
//                            .align(Alignment.TopCenter)
                            .padding(horizontal = 32.dp, vertical = 16.dp)
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
                        ButtonsRow(model, interactions) { transitionToNext(states, interactions, scope) }
                    })
                }
            }
        }
    }

    private fun transitionToNext(
        states: List<MutableTransitionState<Boolean>>,
        view: OnboardingContract.Interactions,
        scope: CoroutineScope
    ) {
        if (!isTransitioning) {
            scope.launch {
                states.reversed().forEach {
                    it.targetState = false
                    delay(50)
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
            enter = fadeIn(animationSpec = tween(durationMillis = 500)),
            exit = fadeOut(animationSpec = tween(durationMillis = 500))
        ) {
            block()
        }
        return state
    }

    @Composable
    private fun ButtonsRow(
        model: OnboardingContract.Model,
        interactions: OnboardingContract.Interactions,
        onClick: () -> Unit
    ) {
        val padding = 16
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
        ) {
            Button(
                modifier = Modifier
                    .padding(bottom = padding.dp, end = padding.dp) // todo position when finished
                    .align(Alignment.BottomEnd),
                border = cuerOutlineButtonStroke(),
                colors = cuerOutlineButtonColors(),
                onClick = onClick
            ) {
                Text(stringResource(if (model.isLastScreen) R.string.finish else R.string.next))
            }

            Text(
                model.screenPosition, modifier = Modifier
                    .padding(bottom = (padding + 12).dp)
                    .align(Alignment.BottomCenter)
            )

            Button(
                modifier = Modifier
                    .padding(bottom = padding.dp, start = padding.dp) // todo position when finished
                    .align(Alignment.BottomStart),
                colors = cuerOutlineButtonColors(),
                onClick = { interactions.onSkip() }
            ) {
                Text(stringResource(R.string.skip))
            }
        }
    }

    @Composable
    private fun Line(
        line: ActionResources,
        isTitle: Boolean,
        modifier: Modifier = Modifier
    ) {
        val vPad = if (isTitle) 32.dp else 8.dp
        Row(
            modifier = modifier
                // .background(Color.Red)
                .padding(bottom = vPad, top = 16.dp)
        ) {
            val color = line.color?.let { colorResource(it) } ?: MaterialTheme.colors.onSurface
            println("line label:" + line.label)
            line.icon?.also {
                Icon(
                    painter = painterResource(it),
                    tint = color,
                    contentDescription = stringResource(id = R.string.menu_search),
                    modifier = Modifier
                        // .background(Color.Green)
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
            style = MaterialTheme.typography.h5,
            color = color,
            text = it
        )
    }
}
