package uk.co.sentinelweb.cuer.app.util.extension

import android.app.Activity
import android.app.Service
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import org.koin.android.ext.android.getKoin
import org.koin.android.scope.AndroidScopeComponent
import org.koin.android.scope.createScope
import org.koin.android.scope.getScopeOrNull
import org.koin.androidx.scope.LifecycleScopeDelegate
import org.koin.core.Koin
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.getScopeId
import org.koin.core.component.getScopeName
import org.koin.core.context.GlobalContext
import org.koin.core.context.KoinContext
import org.koin.core.scope.Scope
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/** copied from org.koin.androidx.scope.FragmentExt but scope wont link as fragment is not attached */
fun <T> ComponentActivity.activityScopeWithSource() = LifecycleScopeWithSourceDelegate<T>(this)

/** copied from org.koin.androidx.scope.FragmentExt but scope wont link as fragment is not attached */
fun <T> Fragment.fragmentScopeWithSource() = LifecycleScopeDelegate<T>(this, getKoin()) { koin: Koin ->
    koin.createScope(getScopeId(), getScopeName(), this)
}

/** links the fragment scope to the activity scope */
fun Fragment.linkScopeToActivity() {
    (this as AndroidScopeComponent).scope.linkTo((requireActivity() as AndroidScopeComponent).scope)
}

fun KoinScopeComponent.linkScopeToAndroidScope(target: AndroidScopeComponent) {
    this.scope.linkTo(target.scope)
}

/** copied from org.koin.android.scope.ServiceExtKt  */
fun Service.serviceScopeWithSource() = lazy { getScopeOrNull() ?: createScope(this) }

/** legacy activity scope (manually destroy)  */
fun Activity.activityLegacyScopeWithSource() = lazy { getScopeOrNull() ?: createScope(this) }

/** wraps org.koin.androidx.scope.LifecycleScopeDelegate - to add source  */
class LifecycleScopeWithSourceDelegate<T>(
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

    private val _lifecycleDelegate =
        LifecycleScopeDelegate<T>(lifecycleOwner, koinContext.get(), createScope)

    override fun getValue(thisRef: LifecycleOwner, property: KProperty<*>): Scope {
        return _lifecycleDelegate.getValue(thisRef, property)
    }
}

fun Scope.getFragmentActivity(): AppCompatActivity =
    (this.get() as Fragment).requireActivity() as AppCompatActivity
