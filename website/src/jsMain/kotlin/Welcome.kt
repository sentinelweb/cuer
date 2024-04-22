import react.Component
import react.Props
import react.ReactNode
import react.State

external interface WelcomeProps : Props {
    var name: String
}

data class WelcomeState(val name: String) : State

class Welcome(props: WelcomeProps) : Component<WelcomeProps, WelcomeState>(props) {

    init {
        state = WelcomeState(props.name)
    }

    // todo port html to kotlin
    override fun render(): ReactNode? = null
}