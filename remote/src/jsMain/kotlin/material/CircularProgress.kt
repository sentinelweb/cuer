@file: JsModule("@material-ui/core/CircularProgress")
@file: JsNonModule

package material

import react.RClass
import react.RProps

external interface CircularProgressProps : RProps {
    var id: String?
    var color: String?
    var variant: String?//"determinate"

}

@JsName("default")
external val CircularProgress: RClass<CircularProgressProps>