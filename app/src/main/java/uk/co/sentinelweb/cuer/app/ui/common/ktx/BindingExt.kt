package uk.co.sentinelweb.cuer.app.ui.common.ktx

import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

fun <T> Fragment.bindObserver(liveData: LiveData<T>, fn: (T) -> Unit) {
    liveData.observe(this.viewLifecycleOwner,
        object : Observer<T> {
            override fun onChanged(model: T) = fn(model)
        }
    )
}