plugins {
//    kotlin("js")
    kotlin("multiplatform")
}
// ./gradlew :website:jsBrowserRun --continue
group = "sentinelweb.cuer"
version = "0.1-alpha"

val outputJsLibName = "cuer_website.js"

repositories {
    mavenCentral()
}

kotlin {
    js(IR) {
        browser {
            binaries.executable()
            commonWebpackConfig {
                //cssSupport.enabled = true
                outputFileName = outputJsLibName
            }
            testTask {
                useKarma {
                    useChromeHeadless()
                }
            }
            runTask {
                devServer = devServer?.copy(port = 3030)
            }
        }
    }
    sourceSets {

        val commonMain by getting {
            dependencies {
                implementation(project(":shared"))
            }
        }

        val jsMain by getting {
            dependencies {
                implementation(libs.kotlinReact)
                implementation(libs.kotlinReactDom)
                implementation(libs.kotlinStyled)
            }
        }
    }
}
