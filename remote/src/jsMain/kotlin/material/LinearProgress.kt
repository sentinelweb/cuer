@file: JsModule("@material-ui/core/LinearProgress")
@file: JsNonModule

package material

import react.RClass
import react.RProps

external interface LinearProgressProps : RProps {
    var id: String?
    var color: String?
    var variant: String? //"determinate"
}

@JsName("default")
external val LinearProgress: RClass<LinearProgressProps>