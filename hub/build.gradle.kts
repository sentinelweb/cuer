import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
}

val ver_jvm: String by project
val ver_koin: String by project

group = "uk.co.sentinlweb.cuer"
version = "1.0-SNAPSHOT"

dependencies {
    // Note, if you develop a library, you should use compose.desktop.common.
    // compose.desktop.currentOs should be used in launcher-sourceSet
    // (in a separate module for demo project and in testMain).
    // With compose.desktop.common you will also lose @Preview functionality
    implementation(project(":shared"))
    implementation(compose.desktop.currentOs)

    // koin
    implementation("io.insert-koin:koin-core:$ver_koin")
//    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.20")
}

compose.desktop {
    application {
        mainClass = "uk.co.sentinelweb.cuer.hub.main.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "test"
            packageVersion = "1.0.0"
        }
    }
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = ver_jvm
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = ver_jvm
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

