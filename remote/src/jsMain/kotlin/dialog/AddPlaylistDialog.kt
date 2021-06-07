package dialog

import com.ccfraser.muirwik.components.MColor
import com.ccfraser.muirwik.components.button.MButtonVariant
import com.ccfraser.muirwik.components.button.mButton
import com.ccfraser.muirwik.components.dialog.*
import com.ccfraser.muirwik.components.form.MFormControlMargin
import com.ccfraser.muirwik.components.mTextField
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.await
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.html.InputType
import org.w3c.dom.HTMLInputElement
import org.w3c.fetch.RequestInit
import org.w3c.xhr.FormData
import playlistItem
import react.*
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.ext.deserialiseResponse

external interface AddPlaylistDialogProps : RProps {
    var isOpen: Boolean
    var close: () -> Unit
    var onConfirm: (PlaylistItemDomain) -> Unit

}

external interface AddPlaylistDialogState : RState {
    var url: String?
    var scanned: MediaDomain?
}

@JsExport
class AddPlaylistDialog : RComponent<AddPlaylistDialogProps, AddPlaylistDialogState>() {

    override fun RBuilder.render() {
        mDialog(props.isOpen, onClose = { _, _ -> onClose() }) {
            mDialogTitle("Add URL")
            mDialogContent {
                state.scanned?.let {
                    playlistItem {
                        video = it
                    }
                } ?: let {
                    mDialogContentText("Paste a YouTube Link ...")
                    mTextField(
                        "Youtube URL",
                        id = "addDialogInputText",
                        autoFocus = true,
                        margin = MFormControlMargin.dense,
                        type = InputType.url,
                        fullWidth = true
                    )
                }
            }
            mDialogActions {
                mButton("Cancel", color = MColor.primary, onClick = { onClose() }, variant = MButtonVariant.text)
                mButton("Check", color = MColor.primary, onClick = { checkUrl() }, variant = MButtonVariant.text)
                mButton("Add", color = MColor.primary, onClick = { validateAndSubmit() }, variant = MButtonVariant.text)
            }
        }
    }

    private fun onClose() {
        props.close()
        setState {
            url = null
            scanned = null
        }
    }

    private fun validateAndSubmit() {
        window.alert("submit")
    }

    private fun checkUrl() {
        (document.getElementById("addDialogInputText") as HTMLInputElement?)
            ?.value
            ?.also { setState { url = it } }
            ?.also {
                MainScope().launch {
                    val checked = checkLink(it)
                    setState { scanned = checked }
                }
            }
    }
}

fun RBuilder.addPlaylistDialog(handler: AddPlaylistDialogProps.() -> Unit) = child(AddPlaylistDialog::class) { attrs(handler) }

suspend fun checkLink(url: String): MediaDomain = coroutineScope {
    val response = window.fetch(
        "/check",
        RequestInit(
            method = "POST",
            body = FormData().apply { append("url", url) }
        )
    ).await()
        .text()
        .await()

    deserialiseResponse(response).let {
        it.payload[0] as MediaDomain
    }
}