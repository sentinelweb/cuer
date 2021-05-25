package uk.co.sentinelweb.cuer.app.util.extension

import android.app.Service
import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import org.koin.android.scope.AndroidScopeComponent
import org.koin.android.scope.createScope
import org.koin.android.scope.getScopeOrNull
import org.koin.androidx.scope.LifecycleScopeDelegate
import org.koin.core.Koin
import org.koin.core.component.getScopeId
import org.koin.core.component.getScopeName
import org.koin.core.context.GlobalContext
import org.koin.core.context.KoinContext
import org.koin.core.scope.Scope
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/** copied from org.koin.androidx.scope.FragmentExt but scope wont link as fragment is not attached */
fun ComponentActivity.activityScopeWithSource() = LifecycleScopeWithSourceDelegate(this)

/** copied from org.koin.androidx.scope.FragmentExt but scope wont link as fragment is not attached */
fun Fragment.fragmentScopeWithSource() = LifecycleScopeDelegate(this) { koin: Koin ->
    koin.createScope(getScopeId(), getScopeName(), this)
}

/** links the fragment scope to the activity scope */
fun Fragment.linkScopeToActivity() {
    (this as AndroidScopeComponent).scope.linkTo((requireActivity() as AndroidScopeComponent).scope)
}

/** copied from org.koin.android.scope.ServiceExtKt  */
fun Service.serviceScopeWithSource() = lazy { getScopeOrNull() ?: createScope(this) }

/** wraps org.koin.androidx.scope.LifecycleScopeDelegate - to add source  */
class LifecycleScopeWithSourceDelegate(
    val lifecycleOwner: LifecycleOwner,
    koinContext: KoinContext = GlobalContext,
    createScope: (Koin) -> Scope = { koin: Koin ->
        koin.createScope(
            lifecycleOwner.getScopeId(),
            lifecycleOwner.getScopeName(),
            lifecycleOwner
        )
    },
) : ReadOnlyProperty<LifecycleOwner, Scope> {

    private val _lifecycleDelegate = LifecycleScopeDelegate(lifecycleOwner, koinContext, createScope)

    override fun getValue(thisRef: LifecycleOwner, property: KProperty<*>): Scope {
        return _lifecycleDelegate.getValue(thisRef, property)
    }
}