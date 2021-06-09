package dialog

import com.ccfraser.muirwik.components.*
import com.ccfraser.muirwik.components.button.MButtonVariant
import com.ccfraser.muirwik.components.button.mButton
import com.ccfraser.muirwik.components.button.mIconButton
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
import react.dom.div
import styled.StyleSheet
import styled.css
import styled.styledDiv
import uk.co.sentinelweb.cuer.core.providers.TimeProvider
import uk.co.sentinelweb.cuer.domain.Domain
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.creator.PlaylistItemCreator
import uk.co.sentinelweb.cuer.domain.ext.deserialiseResponse
import uk.co.sentinelweb.cuer.domain.ext.serialise

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
    var item: PlaylistItemDomain?
    var loading: Visibility
    var snackBarMessage: String?
}

@JsExport
class AddPlaylistDialog : RComponent<AddPlaylistDialogProps, AddPlaylistDialogState>() {

    private val itemCreator: PlaylistItemCreator = PlaylistItemCreator(TimeProvider())
    private val altBuilder = RBuilder()

    override fun AddPlaylistDialogState.init() {
        loading = Visibility.hidden
    }

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
                state.item?.let {
                    mButton(
                        state.selectedPlaylist?.title ?: "Select Playlist",
                        color = MColor.primary,
                        onClick = { setState { playlistsDialogOpen = true } },
                        variant = MButtonVariant.text,
                        disabled = state.item?.id != null
                    ) {
                        css(buttonMargin)
                        attrs.startIcon = mIcon("playlist_add_check", fontSize = MIconFontSize.small, addAsChild = false)
                    }
                    playlistItem {
                        video = it.media
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
                if (state.item == null) {
                    mButton("Check", color = MColor.primary, onClick = { checkUrl() }, variant = MButtonVariant.text)
                } else {
                    mButton(
                        "Add",
                        color = MColor.primary,
                        onClick = { validateAndSubmit() },
                        variant = MButtonVariant.text,
                        disabled = state.item?.id != null
                    )
                }
            }
        }

        playlistListDialog {
            isOpen = playlistsDialogOpen
            close = { setState { playlistsDialogOpen = false } }
            playlists = props.playlists
            onPlayListSelected = { plSelect ->
                setState {
                    selectedPlaylist = plSelect
                    playlistsDialogOpen = false
                    item = item?.copy(playlistId = plSelect?.id)
                }
            }
        }

        mSnackbar(message = state.snackBarMessage ?: "",
            open = state.snackBarMessage != null,
            horizAnchor = MSnackbarHorizAnchor.center,
            vertAnchor = MSnackbarVertAnchor.top,
            autoHideDuration = 4000,
            onClose = { _, reason: MSnackbarOnCloseReason -> /* ?? */ }) {
            attrs.action = altBuilder.div {
                mIconButton("close", onClick = { setState { snackBarMessage = null } }, color = MColor.inherit)
            }
        }

    }

    private fun onClose() {
        resetState()
        props.close()
    }

    private fun resetState() {
        setState {
            url = null
            scanned = null
            item = null
            selectedPlaylist = null
        }
    }

    private fun validateAndSubmit() {
        MainScope().launch {
            state.item?.apply {
                try {
                    val saved = commitItem(this)
                    if (saved.id != null) {
                        onClose()
                    } else {
                        setState { snackBarMessage = "Item was not saved properly" }
                    }
                } catch (e: Exception) {
                    setState { snackBarMessage = e.message }
                }
            }
        }
    }

    private fun checkUrl() {
        (document.getElementById(ADD_DIALOG_INPUT_TEXT_ID) as HTMLInputElement?)
            ?.value
            ?.also { setState { url = it;loading = Visibility.visible } }
            ?.also {
                MainScope().launch {
                    try {
                        val checked = checkLink(it)
                        setState {
                            when (checked) {
                                is MediaDomain -> {
                                    scanned = checked
                                    item = itemCreator.buildPlayListItem(checked, null)
                                }
                                is PlaylistItemDomain -> {
                                    scanned = checked.media
                                    item = checked
                                    selectedPlaylist = checked.playlistId?.let { pid -> props.playlists.find { it.id == pid } }
                                }
                            }
                            loading = Visibility.hidden
                        }
                    } catch (e: Exception) {
                        setState { snackBarMessage = e.message }
                    }
                }
            }
    }

    companion object {
        private const val ADD_DIALOG_INPUT_TEXT_ID = "addDialogInputText"
    }
}

fun RBuilder.addPlaylistDialog(handler: AddPlaylistDialogProps.() -> Unit) = child(AddPlaylistDialog::class) { attrs(handler) }

suspend fun checkLink(url: String): Domain = coroutineScope {
    val response = window.fetch(
        "/checkLink",
        RequestInit(
            method = "POST",
            body = FormData().apply { append("url", url) }
        )
    ).await()
        .text()
        .await()

    deserialiseResponse(response).let {
        if (it.payload.size == 1) {
            it.payload[0]
        } else if (it.errors.size > 0) {
            console.log(it.errors[0].exception) // stopship not for prod!
            throw Exception(it.errors[0].message)
        } else {
            throw Exception("Unexpected payload from checkLink")
        }
    }
}

suspend fun commitItem(item: PlaylistItemDomain): PlaylistItemDomain = coroutineScope {
    val response = window.fetch(
        "/addItem",
        RequestInit(
            method = "POST",
            body = FormData().apply { append("item", item.serialise()) }
        )
    ).await()
        .text()
        .await()

    deserialiseResponse(response).let {
        if (it.payload.size == 1) {
            it.payload[0] as PlaylistItemDomain
        } else if (it.errors.size > 0) {
            console.log(it.errors[0].exception) // stopship not for prod!
            throw Exception(it.errors[0].message)
        } else {
            throw Exception("Unexpected payload from addItem")
        }
    }
}