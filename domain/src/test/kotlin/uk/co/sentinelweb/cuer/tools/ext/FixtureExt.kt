package uk.co.sentinelweb.cuer.tools.ext

import com.flextrade.jfixture.JFixture

// todo figure out how to share this file with test module sources
inline fun <reified T> JFixture.build() = this.create(T::class.java)

inline fun <reified S : Any, reified T : Collection<S>> JFixture.buildCollection(size: Int? = null) =
    size?.let { this.collections().createCollection(T::class.java, S::class.java, it) }
        ?: this.collections().createCollection(T::class.java, S::class.java)