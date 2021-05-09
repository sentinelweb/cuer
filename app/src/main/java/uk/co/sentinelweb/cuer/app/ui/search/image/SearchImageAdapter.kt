package uk.co.sentinelweb.cuer.app.ui.search.image

import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutParams.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import uk.co.sentinelweb.cuer.app.util.firebase.FirebaseDefaultImageProvider
import uk.co.sentinelweb.cuer.app.util.firebase.loadFirebaseOrOtherUrl
import uk.co.sentinelweb.cuer.domain.ImageDomain


class SearchImageAdapter constructor(
    private val imageClick: (ImageDomain) -> Unit,
    private val imageProvider: FirebaseDefaultImageProvider
) : RecyclerView.Adapter<SearchImageAdapter.ImageViewHolder>() {

    private lateinit var recyclerView: RecyclerView

    private var _data: MutableList<ImageDomain> = mutableListOf()

    val data: List<ImageDomain>
        get() = _data

    fun setData(data: List<ImageDomain>) {
        this@SearchImageAdapter._data = data.toMutableList()
        notifyDataSetChanged()
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = recyclerView
    }

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ImageViewHolder {
//        return ImageViewHolder(
//            LayoutInflater.from(parent.context)
//                .inflate(R.layout.view_searchimage_item, parent, false) as ImageView
//        )
        return ImageViewHolder(ImageView(parent.context).apply {
            adjustViewBounds = true
            //layoutParams = RecyclerView.LayoutParams(MATCH_PARENT, WRAP_CONTENT )
        })
    }

    @Override
    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageDomain = _data[position]
        Glide.with(holder.v.context)
            .loadFirebaseOrOtherUrl(imageDomain.url, imageProvider)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(holder.v)
        holder.v.setOnClickListener { imageClick(imageDomain) }
    }

    override fun getItemCount(): Int = _data.size

    class ImageViewHolder(val v: ImageView) : RecyclerView.ViewHolder(v) {

    }
}
