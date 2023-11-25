// build swift: ./gradlew shared:updatePackageSwift
plugins {
    kotlin("multiplatform")
    id("com.android.library")
//    kotlin("com.android.application")
    kotlin("plugin.serialization")
//    id("co.touchlab.faktory.kmmbridge") version "0.2.2"
    kotlin("native.cocoapods")
}

val ver_coroutines: String by project
val ver_kotlinx_serialization_core: String by project
val ver_kotlinx_datetime: String by project
val ver_koin: String by project
val ver_mockk: String by project
val ver_jvm: String by project
//val ver_jfixture: String by project
val ver_truth: String by project
val ver_turbine: String by project
val ver_kotlin_fixture: String by project

val app_compileSdkVersion: String by project
val app_targetSdkVersion: String by project
val app_minSdkVersion: String by project
val app_base: String by project

val ver_swift_tools: String by project
val ver_ios_deploy_target: String by project

group = "uk.co.sentinelweb.cuer"
version = "1.0"

kotlin {
    jvm()
    js(IR) {
        browser()
    }
    androidTarget {
        compilations.all {
            kotlinOptions.jvmTarget = ver_jvm
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    cocoapods {
        framework {
            isStatic = true //or false
        }
        summary = "domain"
        homepage = "https://sentinelweb.co.uk"
        ios.deploymentTarget = ver_ios_deploy_target
    }

    sourceSets {
        all {
            languageSettings.optIn("kotlin.time.ExperimentalTime")
            languageSettings.optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
            languageSettings.optIn("com.russhwolf.settings.ExperimentalSettingsImplementation")
            languageSettings.optIn("kotlinx.serialization.ExperimentalSerializationApi")
        }
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$ver_coroutines")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:$ver_kotlinx_serialization_core")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$ver_kotlinx_serialization_core")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:$ver_kotlinx_datetime")
                implementation("io.insert-koin:koin-core:$ver_koin")
//                implementation("io.ktor:ktor-client-core:$ver_ktor")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation("io.insert-koin:koin-test:$ver_koin")
            }
        }

        val androidMain by getting {
            dependencies {
//                implementation ("io.ktor:ktor-client-cio:$ver_ktor")
//                implementation("com.flextrade.jfixture:jfixture:$ver_jfixture")
                implementation("app.cash.turbine:turbine:$ver_turbine")
            }
        }
        val androidUnitTest by getting {
            dependencies {
                implementation("io.mockk:mockk:$ver_mockk")
                implementation("com.google.truth:truth:$ver_truth")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$ver_coroutines")
                implementation("com.appmattus.fixture:fixture:$ver_kotlin_fixture")

            }
        }
        val jvmMain by getting {
            dependencies {
//                implementation ("io.ktor:ktor-client-cio:$ver_ktor")
            }
        }
        val jvmTest by getting {
            dependencies {
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
            //dependsOn(commonMain)
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
            //dependsOn(commonTest)
            iosX64Test.dependsOn(this)
            iosArm64Test.dependsOn(this)
            iosSimulatorArm64Test.dependsOn(this)
        }
    }
}

android {
    compileSdk = app_compileSdkVersion.toInt()
    namespace = app_base
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = app_minSdkVersion.toInt()
        targetSdk = app_targetSdkVersion.toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}