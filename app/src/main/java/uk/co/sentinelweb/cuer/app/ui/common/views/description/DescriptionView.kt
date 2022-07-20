package uk.co.sentinelweb.cuer.app.ui.common.views.description

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.createScope
import org.koin.core.scope.Scope
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.databinding.PlaylistItemDescriptionBinding
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipCreator
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel
import uk.co.sentinelweb.cuer.app.ui.common.views.description.DescriptionContract.DescriptionModel
import uk.co.sentinelweb.cuer.app.util.glide.GlideFallbackLoadListener
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class DescriptionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle), KoinScopeComponent {

    lateinit var interactions: DescriptionContract.Interactions

    override val scope: Scope by lazy { createScope(this) }

    private val chipCreator: ChipCreator by scope.inject()
    private val res: ResourceWrapper by scope.inject()
    private val log: LogWrapper by scope.inject()

    private val binding: PlaylistItemDescriptionBinding

    private val ytDrawable: Drawable by lazy {
        res.getDrawable(R.drawable.ic_platform_youtube, R.color.primary)
    }

    init {
        binding = PlaylistItemDescriptionBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        binding.pidAuthorImage.setOnClickListener { interactions.onChannelClick() }
        binding.pidDesc.setMovementMethod(object : LinkMovementMethod() {
            override fun handleMovementKey(
                widget: TextView?,
                buffer: Spannable?,
                keyCode: Int,
                movementMetaState: Int,
                event: KeyEvent?
            ): Boolean {
                buffer?.run { interactions.onLinkClick(this.toString()) }
                return true
            }
        })
        binding.pidDesc.setTextIsSelectable(true)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        scope.close()
    }

    fun setModel(model: DescriptionModel) {
        binding.pidDesc.text = model.description
        binding.pidTitle.text = model.title
        binding.pidPubDate.text = model.pubDate
        binding.pidAuthorTitle.text = model.channelTitle
        binding.pidAuthorDesc.text = model.channelDescription
        binding.pidAuthorImage.isVisible = true
        model.channelThumbUrl?.also { url ->
            Glide.with(context)
                .load(url)
                .transition(DrawableTransitionOptions.withCrossFade())
                .addListener(GlideFallbackLoadListener(binding.pidAuthorImage, url, ytDrawable, log))
                .into(binding.pidAuthorImage)
        } ?: run { binding.pidAuthorImage.setImageDrawable(ytDrawable) }

        binding.pidChips.removeAllViews()
        model.playlistChips.forEach { chipModel ->
            chipCreator.create(chipModel, binding.pidChips).apply {
                binding.pidChips.addView(this)
                when (chipModel.type) {
                    ChipModel.Type.PLAYLIST_SELECT -> {
                        setOnClickListener { interactions.onSelectPlaylistChipClick(chipModel) }
                    }
                    ChipModel.Type.PLAYLIST -> {
                        setOnCloseIconClickListener { interactions.onRemovePlaylist(chipModel) }
                    }
                }
            }
        }
    }

    fun channelImageVisible(b: Boolean) {
        binding.pidAuthorImage.isVisible = b
    }

    companion object {
        val viewModule = module {
            scope<DescriptionView> {
                scoped { ChipCreator((getSource() as View).context, get(), get()) }
            }
        }
    }

}