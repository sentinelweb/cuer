import kotlinx.browser.document
import kotlinx.browser.window
import react.dom.render

fun main() {
    window.onload = {
        document.getElementById("root")?.let {
            render(it) {
                child(Welcome::class) {
                    attrs {
                        name = "Kotlin/JS"
                    }
                }
            }
        }
    }
}