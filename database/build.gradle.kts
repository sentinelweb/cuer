plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("com.android.library")
//    kotlin("com.android.application")
    id("com.squareup.sqldelight")
    kotlin("plugin.serialization")
}

group = "uk.co.sentinelweb.cuer"
version = "1.0"

val ver_coroutines: String by project
val ver_kotlinx_serialization_core: String by project
val ver_sqldelight: String by project
val ver_kotlinx_datetime: String by project
val ver_koin: String by project
val ver_turbine: String by project
val ver_kotlin_fixture: String by project
val app_compileSdkVersion: String by project
val app_targetSdkVersion: String by project
val app_minSdkVersion: String by project
val app_base: String by project

val ver_swift_tools: String by project
val ver_ios_deploy_target: String by project

kotlin {
    jvm()
    androidTarget()
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    js(IR) {
        browser()
    }
    cocoapods {
        summary = "Some description for the Shared Module"
        homepage = "Link to the Shared Module homepage"
        ios.deploymentTarget = ver_ios_deploy_target
        version = "1"
        framework {
            baseName = "database"
            isStatic = true //or false
        }
    }

    sourceSets {
        all {
            languageSettings.optIn("kotlin.time.ExperimentalTime")
            languageSettings.optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
        }

        val commonMain by getting {
            dependencies {
                implementation(project(":domain"))
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:$ver_kotlinx_datetime")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$ver_coroutines")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:$ver_kotlinx_serialization_core")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$ver_kotlinx_serialization_core")
                implementation("io.insert-koin:koin-core:$ver_koin")

            }
        }
        val commonTest by getting {
            dependencies {
                implementation(project(":domain"))
                implementation(kotlin("test"))
                implementation("io.insert-koin:koin-test:$ver_koin")
//                implementation("com.flextrade.jfixture:jfixture:$ver_jfixture")
//                implementation("com.google.truth:truth:$ver_truth")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$ver_coroutines")
                implementation("app.cash.turbine:turbine:$ver_turbine")

            }
        }
        val androidMain by getting {
            dependencies {
//                implementation(project(":shared"))
                implementation("com.squareup.sqldelight:android-driver:$ver_sqldelight")
                implementation("io.insert-koin:koin-android:$ver_koin")
            }
        }
        val androidUnitTest by getting {
            dependencies {
                implementation(project(":domain"))
                implementation("io.insert-koin:koin-test-junit4:$ver_koin")
                implementation("com.squareup.sqldelight:sqlite-driver:$ver_sqldelight")
                implementation("com.appmattus.fixture:fixture:$ver_kotlin_fixture")

            }
        }
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
            dependencies {
//                implementation(project(":shared"))
                implementation("com.squareup.sqldelight:native-driver:$ver_sqldelight")
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
        val jsMain by getting {
            dependencies {
                implementation("com.squareup.sqldelight:sqljs-driver:$ver_sqldelight")
            }
        }
        val jsTest by getting
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

// generate schema ./gradlew generateCommonMainDatabaseSchema
// verify schema ./gradlew verifySqlDelightMigration
sqldelight {
    database("Database") { // This will be the name of the generated database class.
        packageName = "uk.co.sentinelweb.cuer.app.db"
        sourceFolders = listOf("sqldelight")
        schemaOutputDirectory = file("src/commonMain/sqldelight/uk/co/sentinelweb/cuer/database/verify")
        verifyMigrations = true
//        deriveSchemaFromMigrations = true
    }
}
