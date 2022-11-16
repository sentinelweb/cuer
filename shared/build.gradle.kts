import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
// build swift: ./gradlew shared:updatePackageSwift
val ver_native_coroutines: String by project
plugins {
    kotlin("multiplatform")
    id("com.android.library")
    kotlin("plugin.serialization")
    id("co.touchlab.faktory.kmmbridge") version "0.2.2"
    kotlin("native.cocoapods")
    id("com.rickclephas.kmp.nativecoroutines") version "0.13.1" //todo use ver_native_coroutines
}

val ver_coroutines: String by project
val ver_kotlinx_serialization_core: String by project
val ver_kotlinx_datetime: String by project
val ver_koin: String by project
val ver_mockk: String by project
val ver_jvm: String by project
val ver_jfixture: String by project
val ver_junit: String by project
val ver_truth: String by project
val ver_mvikotlin: String by project
val ver_kotlinx_coroutines_test: String by project
val ver_multiplatform_settings: String by project
val ver_turbine: String by project
val ver_ktor: String by project
val ver_mockserver: String by project
val app_compileSdkVersion: String by project
val app_targetSdkVersion: String by project
val app_minSdkVersion: String by project
val ver_kotlin_fixture: String by project
val ver_swift_tools: String by project
val ver_ios_deploy_target: String by project

group = "uk.co.sentinelweb.cuer"
version = "1.0"

kotlin {
    jvm()
    js {
        browser()
    }
    android()
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    cocoapods {
        framework {
            isStatic = true //or false
        }
//        name = "shared"
        summary = "shared"
        homepage = "https://sentinelweb.co.uk"
        ios.deploymentTarget = ver_ios_deploy_target
    }

//    targets.withType(org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget::class.java) {
//        binaries.all {
//            binaryOptions["memoryModel"] = "experimental"
//            //freeCompilerArgs += "-Xruntime-logs=gc=info -Xobjc-generics"
//        }
//    }

    sourceSets {
        all {
            languageSettings.optIn("kotlin.time.ExperimentalTime")
            languageSettings.optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
            languageSettings.optIn("com.russhwolf.settings.ExperimentalSettingsImplementation")
            languageSettings.optIn("kotlinx.serialization.ExperimentalSerializationApi")
        }
        val commonMain by getting {
            dependencies {
                implementation(project(":domain"))
                implementation(project(":database"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$ver_coroutines")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:$ver_kotlinx_serialization_core")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$ver_kotlinx_serialization_core")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:$ver_kotlinx_datetime")
                implementation("io.insert-koin:koin-core:$ver_koin")
                implementation("com.arkivanov.mvikotlin:mvikotlin:$ver_mvikotlin")
                implementation("com.arkivanov.mvikotlin:mvikotlin-main:$ver_mvikotlin")
                implementation("com.arkivanov.mvikotlin:mvikotlin-extensions-coroutines:$ver_mvikotlin")
                implementation("com.russhwolf:multiplatform-settings:$ver_multiplatform_settings")
                implementation("com.russhwolf:multiplatform-settings-no-arg:$ver_multiplatform_settings")
                implementation("io.ktor:ktor-client-core:$ver_ktor")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation("io.insert-koin:koin-test:$ver_koin")
                implementation("io.mockk:mockk:$ver_mockk")
            }
        }
        val androidMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-cio:$ver_ktor")
                implementation("io.insert-koin:koin-android:$ver_koin")

            }
        }
        val androidTest by getting {
            dependencies {
                implementation("io.insert-koin:koin-test-junit4:$ver_koin")
                implementation("com.flextrade.jfixture:jfixture:$ver_jfixture")
                implementation("com.appmattus.fixture:fixture:$ver_kotlin_fixture")
                implementation("com.google.truth:truth:$ver_truth")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$ver_coroutines")
                implementation("app.cash.turbine:turbine:$ver_turbine")
                implementation("org.mock-server:mockserver-netty:$ver_mockserver")
                implementation("org.mock-server:mockserver-client-java:$ver_mockserver")
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
    }
}

android {
    compileSdk = app_compileSdkVersion.toInt()
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = app_minSdkVersion.toInt()
        targetSdk = app_targetSdkVersion.toInt()
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
    versionPrefix.set("0.6")
}