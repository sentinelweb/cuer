package dialog

import com.ccfraser.muirwik.components.*
import com.ccfraser.muirwik.components.button.MButtonVariant
import com.ccfraser.muirwik.components.button.mButton
import com.ccfraser.muirwik.components.dialog.*
import com.ccfraser.muirwik.components.form.MFormControlMargin
import dialog.AddPlaylistDialog.ComponentStyles.buttonMargin
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.await
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.css.Visibility
import kotlinx.css.margin
import kotlinx.css.visibility
import kotlinx.html.InputType
import org.w3c.dom.HTMLInputElement
import org.w3c.fetch.RequestInit
import org.w3c.xhr.FormData
import playlistItem
import react.*
import styled.StyleSheet
import styled.css
import styled.styledDiv
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.ext.deserialiseResponse

external interface AddPlaylistDialogProps : RProps {
    var isOpen: Boolean
    var close: () -> Unit
    var onConfirm: (PlaylistItemDomain) -> Unit
    var playlists: List<PlaylistDomain>

}

external interface AddPlaylistDialogState : RState {
    var url: String?
    var scanned: MediaDomain?
    var selectedPlaylist: PlaylistDomain?
    var loading: Visibility
}

@JsExport
class AddPlaylistDialog : RComponent<AddPlaylistDialogProps, AddPlaylistDialogState>() {
    private object ComponentStyles : StyleSheet("ComponentStyles", isStatic = true) {
        val buttonMargin by css {
            margin(1.spacingUnits)
        }
    }

    private var playlistsDialogOpen: Boolean = false

    override fun RBuilder.render() {
        mDialog(props.isOpen, onClose = { _, _ -> onClose() }) {
            mDialogTitle("Add URL")
            styledDiv {
                css {
                    visibility = state.loading
                }
                mLinearProgress(color = MLinearProgressColor.secondary)
            }
            mDialogContent {
                state.scanned?.let {
                    mButton(
                        state.selectedPlaylist?.title ?: "Select Playlist",
                        color = MColor.primary,
                        onClick = { setState { playlistsDialogOpen = true } },
                        variant = MButtonVariant.text,
                    ) {
                        css(buttonMargin)
                        attrs.startIcon = mIcon("playlist_add_check", fontSize = MIconFontSize.small, addAsChild = false)
                    }
                    playlistItem {
                        video = it
                    }
                } ?: let {
                    mDialogContentText("Paste a YouTube Link ...")
                    mTextField(
                        "Youtube URL",
                        id = ADD_DIALOG_INPUT_TEXT_ID,
                        autoFocus = true,
                        margin = MFormControlMargin.dense,
                        type = InputType.url,
                        fullWidth = true
                    )
                }
            }
            mDialogActions {
                mButton("Cancel", color = MColor.primary, onClick = { onClose() }, variant = MButtonVariant.text)
                if (state.scanned == null) {
                    mButton("Check", color = MColor.primary, onClick = { checkUrl() }, variant = MButtonVariant.text)
                } else {
                    mButton("Add", color = MColor.primary, onClick = { validateAndSubmit() }, variant = MButtonVariant.text)
                }
            }
        }
        playlistListDialog {
            isOpen = playlistsDialogOpen
            close = { setState { playlistsDialogOpen = false } }
            playlists = props.playlists
            onPlayListSelected = {
                setState {
                    selectedPlaylist = it
                    playlistsDialogOpen = false
                }
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
        (document.getElementById(ADD_DIALOG_INPUT_TEXT_ID) as HTMLInputElement?)
            ?.value
            ?.also { setState { url = it;loading = Visibility.visible } }
            ?.also {

                MainScope().launch {
                    val checked = checkLink(it)
                    setState {
                        scanned = checked
                        loading = Visibility.hidden
                    }
                }
            }
    }

    override fun AddPlaylistDialogState.init() {
        loading = Visibility.hidden
    }

    companion object {
        private const val ADD_DIALOG_INPUT_TEXT_ID = "addDialogInputText"
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