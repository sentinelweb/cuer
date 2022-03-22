package uk.co.sentinelweb.cuer.domain

sealed class LinkDomain constructor(
    open val address: String,
    open val category: Category = Category.OTHER
) {

    data class UrlLinkDomain(
        override val address: String,
        val title: String? = null,
        val domain: DomainHost = DomainHost.UNKNOWN,
        override val category: Category = Category.OTHER
    ) : LinkDomain(address)

    data class CryptoLinkDomain(
        override val address: String,
        val type: Crypto,
        override val category: Category = Category.SUPPORT
    ) : LinkDomain(address)

    enum class DomainHost(val domains: List<String>) {
        GOOGLE("google.com", "goo.gl"),
        AMAZON("amazon.com", "amzn.to"),
        PATREON("patreon.com"),
        PAYPAL("paypal.com", "paypal.me"),
        APPLE_PODCASTS("podcasts.apple.com"),
        SPOTIFY_PODCASTS("open.spotify.com"),
        BUY_ME_A_COFFEE("buymeacoffee.com"),
        KO_FI("ko-fi.com"),
        FACEBOOK("facebook.com", "fb.me"),
        INSTAGRAM("instagram.com"),
        TWITTER("twitter.com", "t.co"),
        DISCORD("discord.gg"),
        YOUTUBE("youtube.com", "youtu.be"),
        VIMEO("vimeo.com"),
        UNKNOWN();

        constructor(vararg domains: String) : this(domains.toList())
    }

    enum class Category(val hosts: List<DomainHost>) {
        SUPPORT(
            DomainHost.PATREON, DomainHost.PAYPAL, DomainHost.AMAZON, DomainHost.KO_FI,
            DomainHost.BUY_ME_A_COFFEE
        ),
        VIDEO(DomainHost.YOUTUBE, DomainHost.VIMEO),
        SOCIAL(
            DomainHost.TWITTER, DomainHost.FACEBOOK, DomainHost.INSTAGRAM,
            DomainHost.GOOGLE, DomainHost.DISCORD
        ),
        PODCAST(DomainHost.APPLE_PODCASTS, DomainHost.SPOTIFY_PODCASTS),
        OTHER,
        ;

        constructor(vararg hosts: DomainHost) : this(hosts.toList())

        companion object {
            val categoryLookup = values()
                .map { cat -> cat.hosts.map { it to cat } }
                .flatten()
                .associate { it.first to it.second }
        }
    }

    // from https://github.com/k4m4/cryptaddress-validator/blob/master/index.js
    enum class Crypto(val regex: Regex) {
        BITCOIN("\\b[13][a-km-zA-HJ-NP-Z1-9]{25,34}\\b".toRegex()),
        ETHERIUM("\\b0x[a-fA-F0-9]{40}\\b".toRegex()),
        LITECOIN("\\b[L][a-km-zA-HJ-NP-Z1-9]{26,33}\\b".toRegex()),
        DOGE("\\bD{1}[5-9A-HJ-NP-U]{1}[1-9A-HJ-NP-Za-km-z]{32}\\b".toRegex()),
        XMR("\\b4[0-9AB][1-9A-HJ-NP-Za-km-z]{93}\\b".toRegex()),
        XRP("\\br[0-9a-zA-Z]{33}\\b".toRegex()),
        NEO("\\bA[0-9a-zA-Z]{33}\\b".toRegex()),
    }

    companion object {
        fun domain(url: String): String {
            val startIndex = url.indexOf("://") + 3
            val endIndex = url.indexOf("/", startIndex)
                .takeIf { it != -1 }
                ?: url.length
            return url.substring(startIndex, endIndex)
        }
    }
}