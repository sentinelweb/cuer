plugins {
//    kotlin("js")
    kotlin("multiplatform")
}
// ./gradlew :website:jsBrowserRun --continue
group = "sentinelweb.cuer"
version = "0.1-alpha"

val ver_kotlin_react: String by project
val ver_kotlin_styled: String by project

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

                implementation("org.jetbrains.kotlin-wrappers:kotlin-react:$ver_kotlin_react")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom:$ver_kotlin_react")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-styled:$ver_kotlin_styled")
            }
        }
    }
}
//
//tasks {
//    withType<org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack> {
//        outputFileName = outputJsLibName
//    }
//}