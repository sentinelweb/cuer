package uk.co.sentinelweb.cuer.hub.util.extension

import org.koin.core.component.getScopeId
import org.koin.core.component.getScopeName
import org.koin.core.context.GlobalContext

fun <T : DesktopScopeComponent> desktopScopeWithSource(obj: T) =
    GlobalContext.get().run {
        getScopeOrNull(obj.getScopeId())
            ?: createScope(obj.getScopeId(), obj.getScopeName(), obj)
    }

