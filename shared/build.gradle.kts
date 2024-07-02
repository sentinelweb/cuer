import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// build swift release: ./gradlew :shared:updatePackageSwift
// build swift dev: ./gradlew :shared:spmDevBuild
plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.library")
    kotlin("plugin.serialization")
    id("co.touchlab.faktory.kmmbridge") version "0.3.4"
    kotlin("native.cocoapods")
    id("com.rickclephas.kmp.nativecoroutines") version "0.13.3" //todo use ver_native_coroutines
    id("org.jetbrains.compose")
}

group = "uk.co.sentinelweb.cuer"
version = "1.0"

kotlin {
    // fixme should be this
    android {
        compilations.all {
            kotlinOptions.jvmTarget = libs.versions.jvm.get()
        }
    }
    jvm("desktop")
    js(IR) {
        browser()
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    cocoapods {
        framework {
            isStatic = true //or false
            baseName = "shared"
        }
        summary = "shared"
        homepage = "https://sentinelweb.co.uk"
        ios.deploymentTarget = libs.versions.ios.deploy.target.get()
    }

    sourceSets {

        all {
            languageSettings.optIn("kotlin.time.ExperimentalTime")
            languageSettings.optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
            languageSettings.optIn("com.russhwolf.settings.ExperimentalSettingsImplementation")
            languageSettings.optIn("kotlinx.serialization.ExperimentalSerializationApi")
        }
        val desktopMain by getting
        val commonMain by getting {
            dependencies {
                implementation(project(":domain"))
                implementation(project(":database"))

                implementation(libs.kotlinxCoroutinesCore)
                implementation(libs.kotlinxSerializationCore)
                implementation(libs.kotlinxSerializationJson)
                implementation(libs.kotlinxDatetime)
                implementation(libs.koinCore)
                api(libs.mvikotlin)
                api(libs.mvikotlinMain)
                api(libs.mvikotlinExtensionsCoroutines)
                api(libs.essentyLifecycle)
                api(libs.essentyInstanceKeeper)
                implementation(libs.mvikotlinLogging)
                implementation(libs.multiplatformSettings)
                implementation(libs.multiplatformSettingsNoArg)
                implementation(libs.ktorClientCore)

                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)
                println("---------------" + compose.runtime)
                println("---------------" + compose.compiler.auto)
                println("---------------" + compose.components.uiToolingPreview)

            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.koinTest)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(libs.ktorClientCio)
                implementation(libs.koinAndroid)
                implementation(libs.composeUiToolingPreview)
            }
        }
        val androidUnitTest by getting {
            dependencies {
                implementation(libs.koinTestJUnit4)
                implementation(libs.jfixture)
                implementation(libs.kotlinFixture)
                implementation(libs.truth)
                implementation(libs.kotlinxCoroutinesTest)
                implementation(libs.turbine)
                implementation(libs.mockserverNetty)
                implementation(libs.mockserverClientJava)
                implementation(libs.mockkAndroid)
                implementation(libs.mockkAgent)
            }
        }
        val jsMain by getting {
            dependencies {
            }
        }
        val jsTest by getting
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
            dependencies {
                implementation(project(":net"))
            }

        }
        val iosX64Test by getting
        val iosArm64Test by getting
        val iosSimulatorArm64Test by getting
        val iosTest by creating {
            dependsOn(commonTest)
            iosX64Test.dependsOn(this)
            iosArm64Test.dependsOn(this)
            iosSimulatorArm64Test.dependsOn(this)
        }

        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)

        }
    }
}

android {
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    namespace = libs.versions.app.base.get()
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.freeCompilerArgs += "-opt-in=kotlin.time.ExperimentalTime"
}

kmmbridge {
    githubReleaseArtifacts()
    githubReleaseVersions()
    spm()
    //cocoapods("git@github.com:touchlab/PublicPodspecs.git")
    versionPrefix.set("0.6") // fixme do i need this?
}
