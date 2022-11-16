// build swift: ./gradlew shared:updatePackageSwift
plugins {
    kotlin("multiplatform")
    id("com.android.library")
    kotlin("plugin.serialization")
    kotlin("native.cocoapods")
}

val ver_coroutines: String by project
val ver_kotlinx_serialization_core: String by project
val ver_kotlinx_datetime: String by project
val ver_koin: String by project
val ver_ktor: String by project
val ver_mockk: String by project
val ver_jfixture: String by project
val ver_jvm: String by project

val app_compileSdkVersion: String by project
val app_targetSdkVersion: String by project
val app_minSdkVersion: String by project

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
        summary = "net"
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
                implementation(project(":domain"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$ver_coroutines")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:$ver_kotlinx_serialization_core")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$ver_kotlinx_serialization_core")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:$ver_kotlinx_datetime")
                implementation("io.insert-koin:koin-core:$ver_koin")
                implementation("io.ktor:ktor-client-core:$ver_ktor")
//                implementation("io.ktor:ktor-client-cio:$ver_ktor")
//                implementation("io.ktor:ktor-serialization:$ver_ktor")
                implementation("io.ktor:ktor-client-content-negotiation:$ver_ktor")
                implementation("io.ktor:ktor-serialization-kotlinx-json:$ver_ktor")
                implementation("io.ktor:ktor-client-logging:$ver_ktor")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation("io.insert-koin:koin-test:$ver_koin")
            }
        }

        val androidMain by getting {
            dependencies {
            }
        }
        val androidTest by getting {
            dependencies {
                implementation("io.insert-koin:koin-test:$ver_koin")
                implementation("io.mockk:mockk:$ver_mockk")
                implementation("com.flextrade.jfixture:jfixture:$ver_jfixture")
            }
        }
        val jvmMain by getting {
            dependencies {
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