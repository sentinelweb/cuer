import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack

plugins {
    //kotlin()

    id("org.jetbrains.kotlin.js")
}

group = "uk.co.sentinelweb.cuer.remote"
version = "1.0"

repositories {
    maven { setUrl("https://dl.bintray.com/kotlin/kotlin-eap") }
    maven("https://kotlin.bintray.com/kotlin-js-wrappers/")
//    maven("https://dl.bintray.com/cfraser/muirwik")
    mavenCentral()
    jcenter()
}

kotlin {
    js {
        browser {
            commonWebpackConfig {
                cssSupport.enabled = true
            }
        }
        binaries.executable()
    }
}


dependencies {

    implementation(project(":shared"))
    //React, React DOM + Wrappers (chapter 3)
    implementation("org.jetbrains:kotlin-react:17.0.1-pre.148-kotlin-1.4.21")
    implementation("org.jetbrains:kotlin-react-dom:17.0.1-pre.148-kotlin-1.4.21")
    implementation(npm("react", "17.0.1"))
    implementation(npm("react-dom", "17.0.1"))

    //Kotlin Styled (chapter 3)
    implementation("org.jetbrains:kotlin-styled:5.2.1-pre.148-kotlin-1.4.21")
    implementation(npm("styled-components", "~5.2.1"))

    //Video Player (chapter 7)
    implementation(npm("react-youtube-lite", "1.0.1"))

    //Share Buttons (chapter 7)
    implementation(npm("react-share", "~4.2.1"))

    //Coroutines (chapter 8)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.3")

    //implementation("com.ccfraser.muirwik:muirwik-components:0.5.1")

    //https://github.com/CookPete/react-player
    //implementation(npm("react-player", "2.9.0"))

    // material-ui ... doesnt work
    //implementation(npm("@material-ui/core", "4.11.4"))
//    implementation(npm("@material-ui/icons", "4.11.2"))
//    implementation(npm("@material-ui/pickers", "3.3.10"))
//    implementation(npm("@material-ui/styles", "4.11.4"))

}

tasks {
    withType<KotlinWebpack> {
        outputFileName = "cuer.js"
        output.libraryTarget = "commonjs2"
    }
}