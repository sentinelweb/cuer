package uk.co.sentinelweb.cuer.app.ui.browse

import uk.co.sentinelweb.cuer.domain.CategoryDomain
import uk.co.sentinelweb.cuer.domain.ImageDomain
import uk.co.sentinelweb.cuer.domain.ext.makeIds

object BrowseTestData {
    val data = listOf(
        // -1 = build id in loader
        CategoryDomain(
            id = -1,
            title = "Philosophy",
            thumb = ImageDomain("gs://cuer-275020.appspot.com/playlist_header/pexels-matheus-bertelli-2674271-600.jpg"),
            subCategories = listOf(
                CategoryDomain(id = -1, title = "Greek",
                    thumb = ImageDomain(url = "https://d1e4pidl3fu268.cloudfront.net/fb1ef83c-4a76-4d9b-9bd8-609ef1000462/Picture1.crop_573x430_99%2C0.preview.jpg"),
                    subCategories = listOf(
                        CategoryDomain(id = -1,
                            title = "Plato",
                            thumb = ImageDomain(url = "https://i.pinimg.com/originals/5f/eb/34/5feb34c5d9ddf9f4310a850f4a1b1a70.jpg")),
                        CategoryDomain(id = -1,
                            title = "Aristotle",
                            thumb = ImageDomain(url = "https://www.livius.org/site/assets/files/6784/aristotle_khmw.jpg")),
                        CategoryDomain(id = -1,
                            title = "Zeno",
                            thumb = ImageDomain(url = "https://i5.walmartimages.com/asr/053d007b-0267-4068-8b85-87215335f69d_1.812648a49272fc50ed5ef40bd8d53498.jpeg?odnWidth=612&odnHeight=612&odnBg=ffffff")),
                        CategoryDomain(id = -1,
                            title = "Marcus Aurellius",
                            thumb = ImageDomain(url = "https://www.worldhistory.org/img/r/p/750x750/5998.jpg?v=1485682660")),
                    )
                ),
                CategoryDomain(id = -1, title = "Renaissance",
                    thumb = ImageDomain("https://image1.slideserve.com/2111590/slide12-l.jpg"),
                    subCategories = listOf(
                        CategoryDomain(id = -1, title = "Hume"),
                        CategoryDomain(id = -1, title = "Decartes"),
                    )
                ),
                CategoryDomain(id = -1, title = "Eastern",
                    thumb = ImageDomain("https://image1.slideserve.com/2111590/slide12-l.jpg"),
                    subCategories = listOf()
                ),
                CategoryDomain(id = -1, title = "Feminist",
                    thumb = ImageDomain("https://image1.slideserve.com/2111590/slide12-l.jpg"),
                    subCategories = listOf()
                ),
                CategoryDomain(id = -1, title = "Metaphysics",
                    thumb = ImageDomain("https://image1.slideserve.com/2111590/slide12-l.jpg"),
                    subCategories = listOf()
                ),
                CategoryDomain(id = -1, title = "Philosophy of mind",
                    thumb = ImageDomain("https://image1.slideserve.com/2111590/slide12-l.jpg"),
                    subCategories = listOf()
                ),
                CategoryDomain(id = -1, title = "Existentialism",
                    thumb = ImageDomain("https://f1.pngfuel.com/png/519/373/108/mouth-philosophy-being-philosopher-existentialism-subjectivity-ego-aesthetics-png-clip-art.png"),
                    subCategories = listOf(
                        CategoryDomain(id = -1, title = "Kirkegarrd"),
                        CategoryDomain(id = -1, title = "Nietzche"),
                        CategoryDomain(id = -1, title = "Nietzche"),
                        CategoryDomain(id = -1, title = "Heidigger"),
                        CategoryDomain(id = -1, title = "Satre"),
                        CategoryDomain(id = -1, title = "Cam"),
                    )
                ),
                CategoryDomain(id = -1, title = "Poststructuralism",
                    thumb = ImageDomain("https://i1.wp.com/www.hisour.com/wp-content/uploads/2018/06/Post-structuralism.jpg"),
                    subCategories = listOf(
                        CategoryDomain(id = -1, title = "Baurillard"),
                        CategoryDomain(id = -1, title = "Delueze"),
                    )
                ),
            )
        ),
        CategoryDomain(
            id = -1,
            title = "Psychology",
            thumb = ImageDomain("gs://cuer-275020.appspot.com/playlist_header/psychology-4440675_640.jpg"),
            subCategories = listOf(
                CategoryDomain(id = -1,
                    title = "Sigmund Freud",
                    thumb = ImageDomain(url = "https://images.fineartamerica.com/images/artworkimages/mediumlarge/1/sigmund-freud-colorized-20170520-wingsdomain-art-and-photography.jpg")),
                CategoryDomain(id = -1,
                    title = "Jung",
                    thumb = ImageDomain(url = "https://www.lifestreamcoaching.net/wp-content/uploads/2019/08/Carl-Jung.jpg")),
                CategoryDomain(id = -1,
                    title = "Anna Freud",
                    thumb = ImageDomain(url = "https://www.thefamouspeople.com/profiles/images/anna-freud-2.jpg")),
                CategoryDomain(id = -1,
                    title = "Lacan",
                    thumb = ImageDomain(url = "https://laregledujeu.org/files/2015/04/jacques_lacan.jpg")),
            )),

        CategoryDomain(
            id = -1,
            title = "Meditation",
            thumb = ImageDomain("gs://cuer-275020.appspot.com/playlist_header/pexels-emily-hopper-1359000-600.jpg"),
            subCategories = listOf(
                CategoryDomain(id = -1, title = "Vipassana",
                    thumb = ImageDomain(url = "https://i.pinimg.com/originals/ee/44/b6/ee44b6e2e4460d43c635e929ab9f6eaa.jpg")),
                CategoryDomain(id = -1, title = "Zen"),
                CategoryDomain(id = -1, title = "Trancendental"),
                CategoryDomain(id = -1, title = "Chakra"),
                CategoryDomain(id = -1, title = "Yoga"),
                CategoryDomain(id = -1, title = "Jain"),
            )
        )
    ).makeIds()

    val state = BrowseContract.MviStore.State(-1, data)
}