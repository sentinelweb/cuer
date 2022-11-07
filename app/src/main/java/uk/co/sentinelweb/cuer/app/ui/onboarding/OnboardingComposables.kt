package uk.co.sentinelweb.cuer.app.ui.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import uk.co.sentinelweb.cuer.app.ui.common.compose.CuerTheme

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
                Column(modifier = Modifier) {
                    Text(
                        modifier = Modifier.padding(16.dp),
                        text = model.screen.title
                    )
                    Button(
                        modifier = Modifier.padding(16.dp),
                        onClick = { view.onNext() }) {
                        Text("Next")
                    }
                }
            }
        }
    }
}