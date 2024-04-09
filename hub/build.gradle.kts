import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
}

val ver_jvm: String by project
val ver_koin: String by project
val ver_coroutines: String by project
val ver_ktor: String by project
val ver_batik: String by project
val ver_junit: String by project
val ver_mockk: String by project
val ver_turbine: String by project

group = "uk.co.sentinlweb.cuer"
version = "1.0-SNAPSHOT"

dependencies {
    // Note, if you develop a library, you should use compose.desktop.common.
    // compose.desktop.currentOs should be used in launcher-sourceSet
    // (in a separate module for demo project and in testMain).
    // With compose.desktop.common you will also lose @Preview functionality
    implementation(project(":shared"))
    implementation(project(":domain"))
    implementation(project(":remote"))
    implementation(project(":netKmm"))
    implementation(compose.desktop.currentOs)

    // koin
    implementation("io.insert-koin:koin-core:$ver_koin")

    // kotlin coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$ver_coroutines")
    // https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-coroutines-jdk8
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$ver_coroutines")

    // todo upgrade ktor - sec vuln
    implementation("io.ktor:ktor-client-core:$ver_ktor")
    implementation("io.ktor:ktor-client-cio:$ver_ktor")

    implementation("org.apache.xmlgraphics:batik-transcoder:$ver_batik")

    testImplementation("junit:junit:$ver_junit")
    testImplementation("io.insert-koin:koin-test:$ver_koin")
    testImplementation("io.insert-koin:koin-test-junit4:$ver_koin")
    testImplementation("io.mockk:mockk:$ver_mockk")
    testImplementation("app.cash.turbine:turbine:$ver_turbine")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$ver_coroutines")

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

