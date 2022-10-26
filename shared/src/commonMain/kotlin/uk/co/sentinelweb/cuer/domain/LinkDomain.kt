package uk.co.sentinelweb.cuer.domain

sealed class LinkDomain constructor(
    open val address: String,
    open val title: String? = null,
    open val category: Category = Category.OTHER,
    open val extractRegion: Pair<Int, Int>? = null
) {

    data class UrlLinkDomain(
        override val address: String,
        override val title: String? = null,
        val domain: DomainHost = DomainHost.UNKNOWN,
        override val category: Category = Category.OTHER,
        override val extractRegion: Pair<Int, Int>? = null
    ) : LinkDomain(address, title)

    data class CryptoLinkDomain(
        override val address: String,
        override val title: String? = null,
        val type: Crypto,
        override val category: Category = Category.CRYPTO,
        override val extractRegion: Pair<Int, Int>? = null
    ) : LinkDomain(address, category=category, extractRegion = extractRegion)

    enum class DomainHost(val domains: List<String>) {
        GOOGLE("google.com", "goo.gl"),
        AMAZON("amazon.com", "amzn.to"),
        PATREON("patreon.com"),
        PAYPAL("paypal.com", "paypal.me"),
        APPLE_PODCASTS("podcasts.apple.com"),
        SPOTIFY_PODCASTS("open.spotify.com"),
        ANCHOR_FM("anchor.fm"),
        BUY_ME_A_COFFEE("buymeacoffee.com"),
        KO_FI("ko-fi.com"),
        FACEBOOK("facebook.com", "fb.me"),
        INSTAGRAM("instagram.com"),
        TWITTER("twitter.com", "t.co"),
        DISCORD("discord.gg", "discord.io"),
        YOUTUBE("youtube.com", "youtu.be"),
        VIMEO("vimeo.com"),
        SOUNDCLOUD("soundcloud.com"),
        TEESPRING("teespring.com"),
        TWITCH("twitch.tv"),
        WIKIPEDIA("wikipedia.org", "wikimedia.org"),
        UNKNOWN();

        constructor(vararg domains: String) : this(domains.toList())
    }

    enum class Category(val hosts: List<DomainHost>) {
        SUPPORT(
            DomainHost.PATREON, DomainHost.PAYPAL, DomainHost.AMAZON, DomainHost.KO_FI,
            DomainHost.BUY_ME_A_COFFEE, DomainHost.TEESPRING
        ),
        VIDEO(DomainHost.YOUTUBE, DomainHost.VIMEO, DomainHost.TWITCH),
        SOCIAL(
            DomainHost.TWITTER, DomainHost.FACEBOOK, DomainHost.INSTAGRAM,
            DomainHost.GOOGLE, DomainHost.DISCORD
        ),
        PODCAST(
            DomainHost.APPLE_PODCASTS, DomainHost.SPOTIFY_PODCASTS, DomainHost.SOUNDCLOUD,
            DomainHost.ANCHOR_FM
        ),
        CRYPTO,
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

    enum class Crypto(val regex: Regex, name: String) {
        // from https://github.com/k4m4/cryptaddress-validator/blob/master/index.js
        BITCOIN("\\b[13][a-km-zA-HJ-NP-Z1-9]{25,34}\\b".toRegex(), "Bitcoin"),
        ETHERIUM("\\b0x[a-fA-F0-9]{40}\\b".toRegex(), "Etherium"),
        LITECOIN("\\b[L][a-km-zA-HJ-NP-Z1-9]{26,33}\\b".toRegex(), "Litecoin"),
        DOGE("\\bD{1}[5-9A-HJ-NP-U]{1}[1-9A-HJ-NP-Za-km-z]{32}\\b".toRegex(), "Dogecoin"),
        MONERO("\\b4[0-9AB][1-9A-HJ-NP-Za-km-z]{93}\\b".toRegex(), "Monero"),
        RIPPLE("\\br[0-9a-zA-Z]{33}\\b".toRegex(), "Ripple"),
        NEO("\\bA[0-9a-zA-Z]{33}\\b".toRegex(), "Neo"),
        DASH("\\bX[1-9A-HJ-NP-Za-km-z]{33}\\b".toRegex(), "Dash"),
        BITCOIN_CASH("\\b[qp][a-z0-9]{41}\\b".toRegex(), "Bitcoin cash"),
        // https://iohk.zendesk.com/hc/en-us/articles/900005403563-Cardano-address-types
        // https://cardano.stackexchange.com/questions/2370/how-would-i-create-a-regular-expression-to-match-all-of-cardanos-public-wallet
        ADA_SHELLEY("\\baddr1[a-z0-9]+\\b".toRegex(), "ADA"),
        ADA_ICARUS("\\bAe2[1-9A-HJ-NP-Za-km-z]+\\b".toRegex(), "ADA"),
        ADA_DAEDALUS("\\bDdzFF[1-9A-HJ-NP-Za-km-z]+\\b".toRegex(), "ADA"),
        ALGO("\\b[A-Z2-7]{58}\\b".toRegex(),"Algorand"),
        // https://www.reddit.com/r/solana/comments/soi03d/regex_for_validating_solana_addresses/
        // fixme not very good. consider validating with: https://solana-labs.github.io/solana-web3.js/classes/PublicKey.html#isOnCurve
        // or https://security.stackexchange.com/questions/233099/validating-an-ed25519-public-key
        //SOLANA("\\b([1-9A-HJ-NP-Za-km-z]{32,44})\\b".toRegex(),"Solana")//ed25519-public-key
    }
}
