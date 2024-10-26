import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack

// run
// ./gradlew :remote:jsBrowserRun --continue
// ./gradlew :remote:runServer

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    kotlin("plugin.serialization")
    kotlin("native.cocoapods")
}

val outputJsLibName = "cuer_remote.js"

group = "uk.co.sentinelweb.cuer.remote"
version = "1.0"

kotlin {
    jvm()
    js(IR) {
        useCommonJs()
        browser {
            commonWebpackConfig {
                //cssSupport.enabled = true
                outputFileName = outputJsLibName
            }
            runTask {
                // devServer = devServer?.copy(port = 3000)
            }
        }
        binaries.executable()
    }
    androidTarget() {
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
        summary = "remote"
        homepage = "https://sentinelweb.co.uk"
        ios.deploymentTarget = libs.versions.ios.deploy.target.get()
    }

    sourceSets {

        val commonMain by getting {
            dependencies {
                implementation(project(":domain"))
                implementation(project(":database"))
                implementation(project(":shared"))
                implementation(libs.kotlinxDatetime)
                implementation(libs.kotlinxSerializationCore)
                implementation(libs.kotlinxSerializationJson)
                implementation(libs.kotlinxCoroutinesCore)
                implementation(libs.koinCore)
                implementation(libs.ktorClientCore)
            }
        }

        val jvmAndAndroidMain by creating {
            kotlin.srcDir("src/jvmAndAndroid/kotlin")
            dependsOn(commonMain)
            dependencies {
                implementation(libs.logbackClassic)
                implementation(libs.ktorServerCore)
                implementation(libs.ktorServerCio)
                implementation(libs.ktorSerialization)
                implementation(libs.ktorServerCors)
                implementation(libs.ktorServerContentNegotiation)
                implementation(libs.ktorSerializationKotlinxJson)
                implementation(libs.ktorServerCompression)
                implementation(libs.ktorServerCallLogging)
                implementation(libs.ktorServerAuth)
            }
        }

        val jvmMain by getting {
            dependsOn(jvmAndAndroidMain)
        }

        val androidMain by getting {
            dependsOn(jvmAndAndroidMain)
            dependencies {

            }
        }

        val jvmTest by getting {
            dependencies {
                // Koin for JUnit 4
                implementation(libs.koinTestJUnit4)
                implementation(libs.jfixture)
                implementation(libs.truth)
            }
        }

        val jsMain by getting {
            dependencies {
                implementation(libs.kotlinReact)
                implementation(libs.kotlinReactDom)
                implementation(libs.kotlinStyled)
                implementation(npm("react-youtube-lite", "1.1.0"))
                implementation(npm("react-share", "~4.4.0"))
            }
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

val runServer by tasks.creating(JavaExec::class) {
    group = "application"
    mainClass = "uk.co.sentinelweb.cuer.remote.server.MainKt"
    kotlin {
        val main = targets["jvm"].compilations["main"]
        dependsOn(main.compileAllTaskName)
        dependsOn("jsBrowserDevelopmentWebpack")
        val jarTaskName = "jvmJar"
        dependsOn(jarTaskName)
        val jvmJarTask = tasks.getByName<Jar>(jarTaskName)
        classpath(
            { jvmJarTask.outputs.files },
            { configurations["jvmRuntimeClasspath"] }
        )
        println("runServer:" + main.output.allOutputs.files)
    }
}

// include JS artifacts in any JAR we generate
tasks.getByName<Jar>("jvmJar") {
    val taskName = if (project.property("isProduction") == "true") {
        "jsBrowserProductionWebpack"
    } else {
        "jsBrowserDevelopmentWebpack"
    }
    val webpackTask = tasks.getByName<KotlinWebpack>(taskName)
    dependsOn(webpackTask) // make sure JS gets compiled first
    from(File(webpackTask.destinationDirectory, outputJsLibName))
}

tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = libs.versions.jvm.get()
        }
    }
}

