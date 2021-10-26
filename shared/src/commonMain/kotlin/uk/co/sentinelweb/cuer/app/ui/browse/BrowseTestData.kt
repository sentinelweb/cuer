package uk.co.sentinelweb.cuer.app.ui.browse

import uk.co.sentinelweb.cuer.domain.CategoryDomain
import uk.co.sentinelweb.cuer.domain.ImageDomain
import uk.co.sentinelweb.cuer.domain.PlatformDomain
import uk.co.sentinelweb.cuer.domain.ext.buildIdLookup
import uk.co.sentinelweb.cuer.domain.ext.buildParentLookup
import uk.co.sentinelweb.cuer.domain.ext.makeIds
//         File(activity?.cacheDir,"browse.json").writeText(BrowseTestData.data.serialise())
object BrowseTestData {
    val data =
        CategoryDomain(
            id = -1,
            title = "Browse",
            subCategories = listOf(
                // -1 = build id in loader
                CategoryDomain(
                    id = -1,
                    title = "Philosophy",
                    image = ImageDomain("gs://cuer-275020.appspot.com/playlist_header/pexels-matheus-bertelli-2674271-600.jpg"),
                    subCategories = listOf(
                        CategoryDomain(id = -1, title = "Greek",
                            image = ImageDomain(url = "https://d1e4pidl3fu268.cloudfront.net/fb1ef83c-4a76-4d9b-9bd8-609ef1000462/Picture1.crop_573x430_99%2C0.preview.jpg"),
                            subCategories = listOf(
                                CategoryDomain(id = -1,
                                    title = "Socrates",
                                    image = ImageDomain(url = "https://upload.wikimedia.org/wikipedia/commons/thumb/a/a4/Socrates_Louvre.jpg/1200px-Socrates_Louvre.jpg"),
                                    platform = PlatformDomain.YOUTUBE,
                                    platformId = "PLmmblQQ1XpT8QRHv8tBQPfgN8uFy9Joo6"),
                                CategoryDomain(id = -1,
                                    title = "Plato",
                                    image = ImageDomain(url = "https://i.pinimg.com/originals/5f/eb/34/5feb34c5d9ddf9f4310a850f4a1b1a70.jpg"),
                                    platform = PlatformDomain.YOUTUBE,
                                    platformId = "PLmmblQQ1XpT-kNxMgQSOEboS80Dh1xL8L"),
                                CategoryDomain(id = -1,
                                    title = "Aristotle",
                                    image = ImageDomain(url = "https://www.livius.org/site/assets/files/6784/aristotle_khmw.jpg")),
                                CategoryDomain(id = -1,
                                    title = "Zeno",
                                    image = ImageDomain(url = "https://i5.walmartimages.com/asr/053d007b-0267-4068-8b85-87215335f69d_1.812648a49272fc50ed5ef40bd8d53498.jpeg?odnWidth=612&odnHeight=612&odnBg=ffffff")),
                                CategoryDomain(id = -1,
                                    title = "Marcus Aurellius",
                                    image = ImageDomain(url = "https://www.worldhistory.org/img/r/p/750x750/5998.jpg?v=1485682660")),
                            )
                        ),
                        CategoryDomain(id = -1, title = "Renaissance",
                            image = ImageDomain("https://4cawmi2va33i3w6dek1d7y1m-wpengine.netdna-ssl.com/wp-content/uploads/2015/05/11.jpg"),
                            subCategories = listOf(
                                CategoryDomain(id = -1, title = "Hume",
                                    image = ImageDomain("https://4.bp.blogspot.com/-koOp4eiJgI4/VISI_7zq1YI/AAAAAAAAAEs/3lB7hnpF0vY/s1600/David-Hume-philosopher.jpg")),
                                CategoryDomain(id = -1, title = "Decartes",
                                    image = ImageDomain("https://www.bedetheque.com/media/Photos/Photo_46059.jpg")),
                            )
                        ),
                        CategoryDomain(id = -1, title = "Eastern",
                            image = ImageDomain("https://i.pinimg.com/originals/d9/68/92/d96892edbf1da6cc5ee68c2dcba366c8.jpg"),
                            subCategories = listOf()
                        ),
                        CategoryDomain(id = -1, title = "Feminist",
                            image = ImageDomain("https://www.everypainterpaintshimself.com/article_images_new/Mona_Lisa_Large.jpg"),
                            subCategories = listOf()
                        ),
                        CategoryDomain(id = -1, title = "Metaphysics",
                            image = ImageDomain("https://www.danieljoachim.org/wp-content/uploads/2013/07/Metaphysics.jpg"),
                            subCategories = listOf()
                        ),
                        CategoryDomain(id = -1, title = "Philosophy of mind",
                            image = ImageDomain("https://d1gekqscl85idp.cloudfront.net/wisdomtimes/wp-content/uploads/2017/08/09222952/7-Questions-To-Help-You-Understand-The-Philosophy-Of-Mind.jpg"),
                            subCategories = listOf()
                        ),
                        CategoryDomain(id = -1, title = "Existentialism",
                            image = ImageDomain("https://f1.pngfuel.com/png/519/373/108/mouth-philosophy-being-philosopher-existentialism-subjectivity-ego-aesthetics-png-clip-art.png"),
                            subCategories = listOf(
                                CategoryDomain(id = -1, title = "Kirkegarrd",
                                    image = ImageDomain("https://www.philomag.de/sites/default/files/styles/facebook/public/images/210223_kierkegaard-mi_bild-royal-danish-library-public-domain.jpg?itok=PmRmH_Da")),
                                CategoryDomain(id = -1, title = "Nietzche",
                                    image = ImageDomain("https://lh3.googleusercontent.com/-J6DCCZ7t1qo/TYcWAVxy6SI/AAAAAAAAABw/KCH5HQnrtIw/s1600/Nietzsche.jpg")),
                                CategoryDomain(id = -1, title = "Heidigger",
                                    image = ImageDomain("https://i-exc.ccm2.net/iex/1280/1618198816/792172.jpg")),
                                CategoryDomain(id = -1, title = "Satre",
                                    image = ImageDomain("https://miro.medium.com/max/730/1*xky9-_q7PHuaD_qClWwr1Q.jpeg")),
                                CategoryDomain(id = -1, title = "Levinas",
                                    image = ImageDomain("https://cdn.radiofrance.fr/s3/cruiser-production/2018/01/9d982268-c68b-4fc5-b621-e553404d3629/838_gettyimages-98945224.jpg")),
                                CategoryDomain(id = -1, title = "Camus",
                                    image = ImageDomain("https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fwww.hdg.de%2Flemo%2Fimg%2Fgaleriebilder%2Fbiografien%2Fcamus-albert_foto_LEMO-F-4-242_dhm.jpg&f=1&nofb=1")),
                            )
                        ),
                        CategoryDomain(id = -1, title = "Poststructuralism",
                            image = ImageDomain("https://i1.wp.com/www.hisour.com/wp-content/uploads/2018/06/Post-structuralism.jpg"),
                            subCategories = listOf(
                                CategoryDomain(id = -1, title = "Baurillard",
                                    image = ImageDomain("https://www.doppiozero.com/sites/default/files/jean-baudrillard.jpg")),
                                CategoryDomain(id = -1, title = "Delueze",
                                    image = ImageDomain("https://assets.mubicdn.net/images/cast_member/386450/image-original.jpg")),
                            )
                        ),
                    )
                ),
                CategoryDomain(
                    id = -1,
                    title = "Psychology",
                    image = ImageDomain("gs://cuer-275020.appspot.com/playlist_header/psychology-4440675_640.jpg"),
                    subCategories = listOf(
                        CategoryDomain(id = -1,
                            title = "Sigmund Freud",
                            image = ImageDomain(url = "https://images.fineartamerica.com/images/artworkimages/mediumlarge/1/sigmund-freud-colorized-20170520-wingsdomain-art-and-photography.jpg")),
                        CategoryDomain(id = -1,
                            title = "Jung",
                            image = ImageDomain(url = "https://www.lifestreamcoaching.net/wp-content/uploads/2019/08/Carl-Jung.jpg")),
                        CategoryDomain(id = -1,
                            title = "Anna Freud",
                            image = ImageDomain(url = "https://www.thefamouspeople.com/profiles/images/anna-freud-2.jpg")),
                        CategoryDomain(id = -1,
                            title = "Lacan",
                            image = ImageDomain(url = "https://laregledujeu.org/files/2015/04/jacques_lacan.jpg")),
                    )),

                CategoryDomain(
                    id = -1,
                    title = "Meditation",
                    image = ImageDomain("gs://cuer-275020.appspot.com/playlist_header/pexels-emily-hopper-1359000-600.jpg"),
                    subCategories = listOf(
                        CategoryDomain(id = -1, title = "Vipassana",
                            image = ImageDomain(url = "https://i.pinimg.com/originals/ee/44/b6/ee44b6e2e4460d43c635e929ab9f6eaa.jpg")),
                        CategoryDomain(id = -1, title = "Zen",
                            image = ImageDomain(url = "https://d20aeo683mqd6t.cloudfront.net/articles/title_images/000/000/475/medium/16036391499_df20993867_z.jpg?2017")),
                        CategoryDomain(id = -1, title = "Trancendental",
                            image = ImageDomain(url = "https://i0.wp.com/mindisthemaster.com/wp-content/uploads/2020/03/Transcendental-Meditation-Training.jpg?fit=2100%2C1500&ssl=1")),
                        CategoryDomain(id = -1, title = "Chakra",
                            image = ImageDomain(url = "https://cdn-az.allevents.in/events5/banners/2fdbf70d01dbd5b108ecfc5006951b07c6f41bff06f922d823ec541f62391bc8-rimg-w1200-h1200-gmir.jpg?v=1575284947")),
                        CategoryDomain(id = -1, title = "Yoga",
                            image = ImageDomain(url = "https://retreatsinsedona.com/wp-content/uploads/2015/10/hatha-yoga-meditation-sedona-retreat.jpg")),
                        CategoryDomain(id = -1, title = "Jain",
                            image = ImageDomain(url = "https://upload.wikimedia.org/wikipedia/commons/thumb/2/27/Ahinsa_Sthal.jpg/1200px-Ahinsa_Sthal.jpg")),
                    )
                )
            )
        ).makeIds()

    val previewState = BrowseContract.MviStore.State(
        data,
        data.buildIdLookup(),
        data.buildParentLookup()
    )
}