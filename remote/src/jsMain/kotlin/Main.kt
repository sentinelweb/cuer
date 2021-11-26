
import kotlinx.browser.document
import kotlinx.css.*
import react.dom.render
import styled.css
import styled.styledDiv

fun main() {
    document.bgColor = "white"
    render(document.getElementById("root")) {
        styledDiv {
            css {
                fontFamily = "Roboto"
                color = Color.black
                height = 100.pct
            }
            child(App::class) {}
        }
    }

}