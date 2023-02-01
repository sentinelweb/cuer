package uk.co.sentinelweb.cuer.tools.ext

import com.appmattus.kotlinfixture.Fixture
//import com.flextrade.jfixture.JFixture
import uk.co.sentinelweb.cuer.domain.PlaylistDomain

// todo figure out how to share this file with test module sources
//inline fun <reified T> JFixture.build() = this.create(T::class.java)
//
//inline fun <reified S : Any, reified T : Collection<S>> JFixture.buildCollection(size: Int? = null) =
//    size?.let { this.collections().createCollection(T::class.java, S::class.java, it) }
//        ?: this.collections().createCollection(T::class.java, S::class.java)

// sometimes items is empty - which messes up the test
fun generatePlaylist(fixture: Fixture): PlaylistDomain {
    var fixPlaylist = fixture<PlaylistDomain>()
    while (fixPlaylist.items.size == 0) fixPlaylist = fixture()
    return fixPlaylist
}