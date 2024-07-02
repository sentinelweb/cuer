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
val ver_kotlin_fixture: String by project

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
            kotlinOptions.jvmTarget = libs.versions.jvm.get()
        }
    }
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
                implementation("io.ktor:ktor-serialization:$ver_ktor")
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
                implementation("io.ktor:ktor-client-cio:$ver_ktor")

            }
        }
        val androidUnitTest by getting {
            dependencies {
                implementation("io.insert-koin:koin-test:$ver_koin")
                //implementation("io.mockk:mockk:$ver_mockk")
                implementation("io.mockk:mockk-android:$ver_mockk")
                implementation("io.mockk:mockk-agent:$ver_mockk")
                implementation("com.flextrade.jfixture:jfixture:$ver_jfixture")
                implementation("com.appmattus.fixture:fixture:$ver_kotlin_fixture")
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-cio:$ver_ktor")
            }
        }
        val jvmTest by getting {
            dependencies {
            }
        }
        val jsMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-js:$ver_ktor")
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
                implementation("io.ktor:ktor-client-darwin:$ver_ktor")
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