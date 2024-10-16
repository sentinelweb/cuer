package uk.co.sentinelweb.cuer.ui.queue

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import uk.co.sentinelweb.cuer.R


import uk.co.sentinelweb.cuer.ui.queue.QueueFragment.OnListFragmentInteractionListener
import uk.co.sentinelweb.cuer.ui.queue.dummy.Queue.QueueItem

import kotlinx.android.synthetic.main.fragment_item.view.*

/**
 * [RecyclerView.Adapter] that can display a [QueueItem] and makes a call to the
 * specified [OnListFragmentInteractionListener].
 * TODO: Replace the implementation with code for your data type.
 */
class QueueRecyclerViewAdapter(
    private val mValues: List<QueueItem>,
    private val mListener: OnListFragmentInteractionListener?
) : RecyclerView.Adapter<QueueRecyclerViewAdapter.ViewHolder>() {

    private val mOnClickListener: View.OnClickListener

    init {
        mOnClickListener = View.OnClickListener { v ->
            val item = v.tag as QueueItem
            // Notify the active callbacks interface (the activity, if the fragment is attached to
            // one) that an item has been selected.
            mListener?.onListFragmentInteraction(item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mValues[position]
        holder.mIdView.text = item.id
        holder.mContentView.text = item.title
        holder.mUrlView.text = item.url

        with(holder.mView) {
            tag = item
            setOnClickListener(mOnClickListener)
        }
    }

    override fun getItemCount(): Int = mValues.size

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mIdView: TextView = mView.item_id
        val mContentView: TextView = mView.item_title
        val mUrlView: TextView = mView.item_url

        override fun toString(): String {
            return super.toString() + " '" + mContentView.text + "'"
        }
    }
}
