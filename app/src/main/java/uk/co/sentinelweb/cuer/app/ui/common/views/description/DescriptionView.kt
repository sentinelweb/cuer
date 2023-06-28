package uk.co.sentinelweb.cuer.app.ui.common.views.description

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.AttributeSet
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
import uk.co.sentinelweb.cuer.app.databinding.ViewDescriptionBinding
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipCreator
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel
import uk.co.sentinelweb.cuer.app.ui.common.ribbon.RibbonItemView
import uk.co.sentinelweb.cuer.app.ui.common.views.description.DescriptionContract.DescriptionModel
import uk.co.sentinelweb.cuer.app.util.glide.GlideFallbackLoadListener
import uk.co.sentinelweb.cuer.app.util.link.LinkExtractor
import uk.co.sentinelweb.cuer.app.util.link.TimecodeExtractor
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.LinkDomain
import uk.co.sentinelweb.cuer.domain.TimecodeDomain


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
    private val linkExtractor: LinkExtractor by scope.inject()
    private val timecodeExtractor: TimecodeExtractor by scope.inject()

    private val binding: ViewDescriptionBinding

    lateinit var ribbonItems: List<RibbonItemView>
        private set

    var playlistChipsUpdate: String? = null

    private val ytDrawable: Drawable by lazy {
        res.getDrawable(R.drawable.ic_platform_youtube, R.color.primary)
    }

    init {
        binding = ViewDescriptionBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        binding.pidAuthorImage.setOnClickListener { interactions.onChannelClick() }
        binding.pidDesc.setTextIsSelectable(true)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        scope.close()
    }

    fun setModel(model: DescriptionModel) {
        binding.pidDesc.text = model.description
        manualLink(binding.pidDesc)
        binding.pidTitle.text = model.title
        binding.pidPubDate.text = model.pubDate
        binding.pidAuthorTitle.text = model.channelTitle
        binding.pidAuthorDesc.text = model.channelDescription
        binding.pidAuthorImage.isVisible = true
        setInfo(model, binding.pidInfo)
        model.channelThumbUrl
            ?.also { url ->
                Glide.with(context)
                    .load(url)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .addListener(
                        GlideFallbackLoadListener(binding.pidAuthorImage, url, ytDrawable, log)
                    )
                    .into(binding.pidAuthorImage)
            }
            ?: run { binding.pidAuthorImage.setImageDrawable(ytDrawable) }

        val playlistChipsState = model.playlistChips.joinToString { it.text }
        if (playlistChipsUpdate != playlistChipsState) {
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
                            setOnClickListener { interactions.onPlaylistChipClick(chipModel) }
                        }
                    }
                }
            }
            playlistChipsUpdate = playlistChipsState
        }
        if (!this::ribbonItems.isInitialized ||
            ribbonItems.map { it.item.type } != model.ribbonActions.map { it.type }
        ) {
            binding.pidRibbonCtnr.removeAllViews()
            ribbonItems = model.ribbonActions.map {
                RibbonItemView(it, binding.pidRibbonCtnr)
                    .apply { setOnClickListener { item -> interactions.onRibbonItemClick(item) } }
            }
        }
    }

    private fun manualLink(tv: TextView) {
        val text = tv.text.toString()
        val links = linkExtractor.extractLinks(text)
        val timecodes = timecodeExtractor.extractTimecodes(text)
        val textWithLinks = SpannableString(tv.text)
        links
            .forEach { link ->
                val span = InternalURLSpan(link) {
                    when (it) {
                        is LinkDomain.UrlLinkDomain -> interactions.onLinkClick(it)
                        is LinkDomain.CryptoLinkDomain -> interactions.onCryptoClick(it)
                    }
                }
                textWithLinks.setSpan(
                    span,
                    link.extractRegion!!.first,
                    link.extractRegion!!.second + 1,
                    Spanned.SPAN_EXCLUSIVE_INCLUSIVE
                )
            }
        timecodes
            .forEach { timecode ->
                val span = InternalTimecodeSpan(timecode) {
                    interactions.onTimecodeClick(it)
                }
                textWithLinks.setSpan(
                    span,
                    timecode.extractRegion!!.first,
                    timecode.extractRegion!!.second + 1,
                    Spanned.SPAN_EXCLUSIVE_INCLUSIVE
                )
            }

        tv.text = textWithLinks
        tv.linksClickable = true
        tv.movementMethod = LinkMovementMethod.getInstance()
        tv.setTextIsSelectable(true)
    }

    @SuppressLint("SetTextI18n")
    private fun setInfo(model: DescriptionModel, pidInfo: TextView) {
        pidInfo.text = """
            ID: ${model.info.platformId} (${model.info.platform})
            DBID: ${model.info.dbId}
            Broadcast: ${model.info.broadcastDate}
        """.trimIndent()
    }

    class InternalURLSpan(
        private var link: LinkDomain,
        private var clickInterface: (LinkDomain) -> Unit
    ) : ClickableSpan() {
        override fun onClick(widget: View) {
            clickInterface(link)
        }
    }

    class InternalTimecodeSpan(
        private var timecode: TimecodeDomain,
        private var clickInterface: (TimecodeDomain) -> Unit
    ) : ClickableSpan() {
        override fun onClick(widget: View) {
            clickInterface(timecode)
        }
    }

    fun channelImageVisible(b: Boolean) {
        binding.pidAuthorImage.isVisible = b
    }

    companion object {
        val viewModule = module {
            scope<DescriptionView> {
                scoped { ChipCreator((get() as View).context, get(), get()) }
            }
        }
    }

}