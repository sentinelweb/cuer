package uk.co.sentinelweb.cuer.app.ui.common.ktx

import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

fun <T> Fragment.bindObserver(liveData: LiveData<T>, fn: (T) -> Unit) {
    liveData.observe(this.viewLifecycleOwner,
        object : Observer<T> {
            override fun onChanged(model: T) = fn(model)
        }
    )
}

fun <T> Fragment.bindFlow(flow: Flow<T>, fn: (T) -> Unit) {
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            flow.collectLatest {
                fn(it)
            }
        }
    }
}