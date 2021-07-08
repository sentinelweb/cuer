plugins {
    kotlin("js")
}

group = "sentinelweb.cuer"
version = "0.1-alpha"

val ver_kotlin_react: String by project
val ver_kotlin_styled: String by project

val outputJsLibName = "cuer_website.js"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":shared"))
    implementation("org.jetbrains.kotlin-wrappers:kotlin-react:$ver_kotlin_react")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom:$ver_kotlin_react")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-styled:$ver_kotlin_styled")

    testImplementation(kotlin("test-js"))

}

kotlin {
    js {
        browser {
            binaries.executable()
            commonWebpackConfig {
                cssSupport.enabled = true
            }
            testTask {
                useKarma {
                    useChromeHeadless()
                }
            }
        }
    }
}

tasks {
    withType<org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack> {
        outputFileName = outputJsLibName
    }
}