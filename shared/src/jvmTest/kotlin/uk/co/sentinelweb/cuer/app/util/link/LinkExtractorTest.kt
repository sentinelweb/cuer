package uk.co.sentinelweb.cuer.app.util.link

import org.junit.Test
import uk.co.sentinelweb.cuer.domain.LinkDomain
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LinkExtractorTest {

    private val sut = LinkExtractor()

    @Test
    fun extractLinks_then_and_now() {
        val actual = sut.extractLinks(THEN_AND_NOW_DESC)
        actual.forEach { println(it) }
    }

    @Test
    fun extractLinks_einzelganger() {
        val actual = sut.extractLinks(EINZELGANGER_DESC)
        actual.forEach { println(it) }
        actual
            .find { it is LinkDomain.CryptoLinkDomain && it.type==LinkDomain.Crypto.BITCOIN }
            ?.apply { assertEquals("✔️ Bitcoin: ", title) }

    }

    @Test
    fun extractLinks_just_have_a_think() {
        val actual = sut.extractLinks(JUST_HAVE_A_THINK_DESC)
        actual.forEach { println(it) }
    }

    @Test
    fun extractLinks_crypto() {
        val actual = sut.extractLinks(CRYPTO1_DESC)
        actual.forEach { println(it) }
        actual
            .find { it is LinkDomain.CryptoLinkDomain && it.type==LinkDomain.Crypto.BITCOIN }
            ?.apply { assertEquals("\$BTC - ", title) }
        actual
            .find { it is LinkDomain.CryptoLinkDomain && it.type==LinkDomain.Crypto.XRP }
            ?.apply { assertEquals("\$XRP - ", title) }
//        actual
//            .find { it is LinkDomain.CryptoLinkDomain && it.type==LinkDomain.Crypto.BITCOIN }
//            ?.apply { assertEquals("\$BTC - ", title) }
//        actual
//            .find { it is LinkDomain.CryptoLinkDomain && it.type==LinkDomain.Crypto.BITCOIN }
//            ?.apply { assertEquals("\$BTC - ", title) }
    }

    @Test
    fun extractLinks_coinbureau() {
        val actual = sut.extractLinks(COINBUREAU_DESC)
        actual.forEach { println(it) }
    }

    companion object {
        const val THEN_AND_NOW_DESC = """
            Earth, we have a problem.

Then & Now is FAN-FUNDED! Support me on Patreon and pledge as little as $1 per video: http://patreon.com/user?u=3517018

Or send me a one-off tip of any amount and help me make more videos:

https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=JJ76W4CZ2A8J2

Buy on Amazon through this link to support the channel:

https://amzn.to/2ykJe6L 

Follow me on:

Facebook: http://fb.me/thethenandnow
Instagram: https://www.instagram.com/thethenandnow/
Twitter: https://twitter.com/lewlewwaller

Subscribe to the podcast:

https://podcasts.apple.com/gb/podcast/then-now-philosophy-history-politics/id1499254204

https://open.spotify.com/show/1Khac2ih0UYUtuIJEWL47z

Description:

What does 'meta' really mean? What can we make of Facebook's change to 'Meta'? Jean-Francois Lyotard wrote about the decline of metanarratives in 1979's 'The Postmodern Condition: A Report on Knowledge'. Can we learn anything about Zuckerberg's aspirations from this classic postmodern text?

Lyotard was prescient. He noticed in the 70s that quote ‘the miniaturization and commercialization of machines is already changing the way in which learning is acquired, classified, made available, and exploited.’ He also that  ‘Knowledge is and will be produced in order to be sold, it is and will be consumed in order to be valorized in a new production: in both cases, the goal is exchange.’

Sources:

https://edition.cnn.com/2021/10/25/business/ethiopia-violence-facebook-papers-cmd-intl/index.html 

https://www.vox.com/recode/22799665/facebook-metaverse-meta-zuckerberg-oculus-vr-ar 

https://theconversation.com/we-know-better-than-to-allow-facebook-to-control-the-metaverse-171467 

https://tech.fb.com/connect-2021-our-vision-for-the-metaverse/ 

https://www.washingtonpost.com/technology/2021/10/25/what-are-the-facebook-papers/ 

https://apnews.com/article/the-facebook-papers-whistleblower-misinfo-trafficking-64f11ccae637cdfb7a89e049c5095dca 

https://edition.cnn.com/2021/10/22/business/january-6-insurrection-facebook-papers/index.html 

Hans Bertens, The Idea of the Postmodern

Jean-Francois Lyotard, The Postmodern Condition 

Ethan Zuckerman, The Case for Digital Public Infrastructure, https://knightcolumbia.org/content/the-case-for-digital-public-infrastructure 

#metaverse #zuckerberg #facebook
CREDITS

Lyotard Photo: Bracha L. Ettinger, CC BY-SA 2.5, https://creativecommons.org/licenses/by-sa/2.5, via Wikimedia Commons
        """

        const val EINZELGANGER_DESC = """
            The ancient Stoics based their morals on reason. They created an ethical system that focuses on living in agreement with nature, or, a universal rational principle. Stoic ethics distinguish virtues from vices, seeing a life of virtue as the optimal way to live. From the Stoic point of view, someone who lives a life of vice can be considered a degenerate.

This video explores a couple of examples from Stoic literature on how the ancient Stoics saw degeneracy or, put differently, a bad and sub-par way of life leading to unhappiness.

Support the channel:
✔️ PayPal: https://www.paypal.me/einzelgangerco
✔️ Patreon: https://www.patreon.com/einzelgangster
✔️ Bitcoin: 3HQnEz1LQ4G6dqN2LdZgzc7qoJjJCyWjTC
(Donated BTC and want in the credits? Send me an email with the amount/address)

🎞️ Animations and thumbnail art by Munkaa:
https://www.youtube.com/user/jus7y

Purchase Stoicism for Inner Peace (affiliate links):
📘 Paperback: https://amzn.to/3wB9iHb
💻 Ebook: https://amzn.to/2RdjbKV

Purchase Unoffendable (affiliate links):
📘 Paperback: https://amzn.to/2RJYfti
💻 Ebook: https://amzn.to/35e23a5

Merchandise:
🛍️ Shop: teespring.com/stores/einzelgangerstore
🛍️ Merchandise design by Punksthetic Art:
https://www.youtube.com/user/JRStoneart

Wikimedia Commons (attributions):

Zeus painting (1): https://commons.wikimedia.org/wiki/File:Wall_painting_-_Zeus_and_Eros_-_Herculaneum_(ins_or_II_basilica-augusteum)_-_Napoli_MAN_9553.jpg
Zeus painting (2): https://commons.wikimedia.org/wiki/File:Villa_Nichesola-Conforti,_Ponton_di_Sant%27Ambrogio_di_Valpolicella_(VR)._Sala_Rossa_12.jpg
Zeus painting (3): https://commons.wikimedia.org/wiki/File:Moscow_Spiridonovka_Tarasov_Mansion_asv2021-07_img16.jpg
Zeus pot: https://commons.wikimedia.org/wiki/File:Calyx-krater_olympian_assembly_MAN.jpg
Zeus statue: https://commons.wikimedia.org/wiki/File:Paris-33_(29998023800).jpg
Seneca (1): https://commons.wikimedia.org/wiki/File:A_man_attended_by_a_surgeon_(suicide_of_Seneca%3F)._Oil_painti_Wellcome_V0017316.jpg
Pseudo-Seneca: https://commons.wikimedia.org/wiki/File:Bust_of_Pseudo-Seneca_MET_rl1975.1.843.R.jpg
Seneca bust: https://commons.wikimedia.org/wiki/File:%D0%A1%D0%B0%D0%BD%D0%BA%D1%82-%D0%9F%D0%B5%D1%82%D0%B5%D1%80%D0%B1%D1%83%D1%80%D0%B3,_%D0%9B%D0%B5%D1%82%D0%BD%D0%B8%D0%B9_%D1%81%D0%B0%D0%B4._%D0%91%D1%8E%D1%81%D1%82_%C2%AB%D0%A1%D0%B5%D0%BD%D0%B5%D0%BA%D0%B0%C2%BB_2.jpg
Epictetus: https://commons.wikimedia.org/wiki/File:Discourses_-_Epictetus_(illustration_1)_(9021700938).jpg
Marcus Aurelius statue: https://commons.wikimedia.org/wiki/File:Equestrian_statue_Marcus_Aurelius_replica,_Capitole,_Rome,_Italy.jpg 
Marcus Aurelius bust (1): https://commons.wikimedia.org/wiki/File:MarkAurelFront.jpg
Marcus Aurelius bust (2): https://commons.wikimedia.org/wiki/File:L%27Image_et_le_Pouvoir_-_Buste_cuirass%C3%A9_de_Marc_Aur%C3%A8le_ag%C3%A9_-_2.jpg
Marcus Aurelius bust (3): https://commons.wikimedia.org/wiki/File:Stockholm_Royal_Palace_bust_Marcus_Aurelius.jpg 
Marcus Aurelius bust (4): https://commons.wikimedia.org/wiki/File:Marcus_Aurelius_(32393792365).jpg
Rome: https://commons.wikimedia.org/wiki/File:Roman_Bath_(SM_27).png

#STOICISM #EPICTETUS #MARCUSAURELIUS

00:00 - Intro
02:30 - Living in agreement with Zeus
04:01 - Fighting fate
05:50 - The man who cheated
07:54 - Laying in bed all day
09:46 - Sickly pale of lust
            """

        const val JUST_HAVE_A_THINK_DESC = """
            Can we survive the coming decades? Given the state of global affairs today, that's probably a question many people are asking themselves right now. The IPCC has just published their answer, at least from a climate point of view. And they pull no punches. We're almost out of time, they say, and we need immediate global geopolitical cooperation to succeed. Oh dear!

Help support this channels independence at
http://www.patreon.com/justhaveathink

Or with a donation via Paypal by clicking here
https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=GWR73EHXGJMAE&source=url

You can also help keep my brain ticking over during the long hours of research and editing via the nice folks at BuyMeACoffee.com
https://www.buymeacoffee.com/justhaveathink

Video Transcripts available at our website
http://www.justhaveathink.com

Interested in mastering and remembering the concepts that I present in my videos? Check out the FREE Dive Deeper mini-courses offered by the Center for Behavior and Climate. These mini-courses teach the main concepts in select JHAT videos and go beyond to help you learn additional scientific or conservation concepts. The courses are great for teachers to use or for individual learning.https://climatechange.behaviordevelopmentsolutions.com/just-have-a-think-jhat

IPCC Working Group 2 report
https://report.ipcc.ch/ar6wg2/pdf/IPCC_AR6_WGII_SummaryForPolicymakers.pdf

Parag Khanna - Earth at 4 degrees
https://www.paragkhanna.com/2016-3-9-the-world-4-degrees-warmer/

Check out other YouTube Climate Communicators

zentouro: 
https://www.youtube.com/user/zentouro

Climate Adam: 
https://www.youtube.com/user/ClimateAdam

Kurtis Baute:
https://www.youtube.com/user/ScopeofScience

Levi Hildebrand: 
https://www.youtube.com/user/The100LH

Simon Clark:
https://www.youtube.com/user/SimonOxfPhys

Sarah Karvner:
https://www.youtube.com/channel/UCRwMkTu8sCwOOD6_7QYrZnw

Rollie Williams / ClimateTown: https://www.youtube.com/channel/UCuVLG9pThvBABcYCm7pkNkA

Jack Harries:
https://www.youtube.com/user/JacksGap

Beckisphere: https://www.youtube.com/channel/UCT39HQq5eDKonaUV8ujiBCQ

Our Changing Climate :
https://www.youtube.com/channel/UCNXvxXpDJXp-mZu3pFMzYHQ, published=2022-03-06T14:00:01, channelData=ChannelDomain(id=null, platformId=UCRBwLPbXGsI2cJe9W1zfSjQ, platform=YOUTUBE, country=null, title=Just Have a Think, customUrl=https://youtube.com/channel/justhaveathink, description=This channel seeks to  understand the issues that face our civilisation in the 21st Century and focusses on the potential solutions that will save as many lives as possible and hopefully bring about a greater level of equality in the world. The channel is not a debating forum about whether Human Induced Climate Change is a real phenomenon or not. If that's what you're after then I can highly recommend chat forums on Social Media, where people on both sides of the argument go round and round in circles achieving precisely nothing at all.

That is a mug's game. And it's not my game. 

Anyway, outside of those caveats, I do try to keep it light and humorous wherever possible, so I hope you enjoy the content. 

Oh..and me... Dave Borlace. Born 1969. BSc in Technology from the Open University in the UK. After a 30 year career in People and Project Management,  I now work full time on the channel thanks to the amazing folks who support me on Patreon http://www.patreon.com/justhaveathink
        """

        const val CRYPTO1_DESC = """
            Hello, I am NCashOfficial delivering you the hard truth with technical analysis without any fake hopium. I make strong and unwanted calls that are highly probable. Don’t get caught in the bear traps, stay strong and HODL on through the moonboy/girl hopium.

✅ My Exit Strategies, Portfolio Insights, & MUCH MORE:
https://www.ncashofficial.com

✅ Official Crypto Posters - https://officialcryptoposters.com/

✅ Join this channel to get access to exclusive member only perks, including specific insights into my crypto holdings/portfolio:
https://www.youtube.com/channel/UC4PZGdFS6D9j3r8pQWBNSww/join

✅ KEEP YOUR CRYPTO SECURE -
https://shop.ledger.com/pages/ledger-nano-x?r=3c8c376d6cdb&tracker=MY_TRACKER

✅ Referral Links -
https://t.co/R2O8tLCKsQ?amp=1
https://t.co/Bs9jqgyFuS?amp=1

✅ Discord Server -
https://discord.gg/JMH4GYY5ce 

✅ Make sure to stay on top of the market by following me on Twitter – http://www.twitter.com/NCashOfficial

I do not have any other social media accounts, I would never ask for you to send me crypto in exchange for information, I would never ask for any logins, etc. Please be cautious about sharing any information in this market as scamming is a BIG issue.

If you want to send me crypto you can at these addresses

${'$'}BCH - qqeugcrwctrp7xgnqlue3py75qr377zgfgnhzarspg
${'$'}HBAR - 0.0.38674 / 101598345
${'$'}DNT - 0x49a2F63e2786cA59008210c5D97086764bff1343
${'$'}ALGO - XWYE2BM5HC7OPT4ICTAIYEHPQJVX74ZXN2U7BOEUI7VSELQAOY4ACUWGO4
${'$'}BTC - 3BfsmJP4PvzKTMYDwFT5h33PWMbf6g1XnV
${'$'}ETH - 0xd8B899c8A3f537F84E4661Fa9BE758FF19078637
${'$'}LINK - 0x0bDFf0EF1bA2e4F2eC1299a32350aF4007c18f99
${'$'}REN - 0x74C2D3bFe62c0b27C2854C3FdEBD9C0d9e6E2b8E
${'$'}XRP - rMdG3ju8pgyVh29ELPWaDuA74CpWW6Fxns TAG: 357034853
${'$'}XLM - GDQP2KPQGKIHYJGXNUIYOMHARUARCA7DJT5FO2FFOOKY3B2WSQHG4W37 TAG: 2258476868

DISCLAIMER: I am not a financial adviser, & all information given in my videos or on my social media platforms is for entertainment purposes only and is not financial advice. Investing/trading is a risk that is your own responsibility. You can easily lose your money in this market. None of my information should be used to make any investment decisions. Thank you all for the continued support, I appreciate you all.

Description Tags (Ignore) 
#hbar #hedera #crypto
        """
//        const val _DESC = """
//        """
//        const val _DESC = """
//        """
//        const val _DESC = """
//        """

    private val COINBUREAU_DESC = """🛒 Best Deals in Crypto 👉 https://guy.coinbureau.com/deals/
📲 Insider Info in my Socials 👉 https://guy.coinbureau.com/socials/
👕 My Merch Store 👉 https://store.coinbureau.com
🔥 TOP Crypto TIPS In My Newsletter 👉 https://guy.coinbureau.com/signup/

~~~~~~

📺 Useful Vids 📺 

Sanctions 👉 https://www.youtube.com/watch?v=hOjbqMdqZRU
Supply Chains 👉 https://www.youtube.com/watch?v=-5RPwo0dlek

~~~~~~

⛓️ 🔗 Useful Links 🔗 ⛓️

Europe's Nuclear Problem: https://www.bloomberg.com/news/features/2022-01-31/europe-s-nuclear-power-plants-are-disappearing-just-as-energy-crisis-hits-hard#xj4y7vzkg 
Green Energy Issues: https://www.reuters.com/markets/commodities/weak-winds-worsened-europes-power-crunch-utilities-need-better-storage-2021-12-22/
Lehman Like Moment: https://www.businessinsider.com/germany-says-energy-crisis-may-trigger-lehman-like-contagion-2022-6
Russian Gas: https://www.ft.com/content/c0398409-56f7-4924-a4cb-dfb0860c447d
Underinvestment: https://fortune.com/2022/06/27/exxonmobil-ceo-blames-europes-energy-crisis-on-underinvestment-in-the-oil-and-gas-industry/
Closing Plants: https://www.washingtonpost.com/opinions/2022/01/01/germany-is-closing-its-last-nuclear-plants-what-disaster/
Europe's Energy Conundrum: https://fortune.com/2022/06/27/exxonmobil-ceo-blames-europes-energy-crisis-on-underinvestment-in-the-oil-and-gas-industry/
Reclassify: https://www.ft.com/content/0df04289-1014-406e-81c7-1e4a6b1ea5bc
Using Coal: https://www.washingtonpost.com/world/2022/06/22/coal-plant-europe-germany-austria-netherlands-russia-gas/

~~~~~~

- TIMESTAMPS -
0:00 Intro
1:28 Background
4:16 Misconceptions
6:40 Issues With “Green” Energy
10:23 Gas Dependence
13:08 Sanctions & Energy
16:46 Panic Sets in
18:53 Conclusion
 
~~~~~~~

📜 Disclaimer 📜

The information contained herein is for informational purposes only. Nothing herein shall be construed to be financial legal or tax advice. The content of this video is solely the opinions of the speaker who is not a licensed financial advisor or registered investment advisor. Trading cryptocurrencies poses considerable risk of loss. The speaker does not guarantee any particular outcome.

#Energy #Europe #Gas #Crisis #economics
    """.trimIndent()
    }
}