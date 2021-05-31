import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack

// ./gradlew :remote:jsBrowserRun --continue

//  ./gradlew :remote:runServer

plugins {
    kotlin("multiplatform")
    application
    kotlin("plugin.serialization")
}

val ver_kotlinx_datetime: String by project
val ver_kotlinx_serialization_core: String by project
val ver_ktor: String by project
val ver_jvm: String by project
val outputJsLibName = "cuer.js"

group = "uk.co.sentinelweb.cuer.remote"
version = "1.0"

kotlin {
    js {
        browser {
            commonWebpackConfig {
                cssSupport.enabled = true
            }
        }
        binaries.executable()
    }
    jvm {
        //withJava()
    }
    sourceSets {

        val commonMain by getting {
            dependencies {
                implementation(project(":shared"))
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:$ver_kotlinx_datetime")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:$ver_kotlinx_serialization_core")
                implementation("io.ktor:ktor-client-core:$ver_ktor")
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:$ver_kotlinx_serialization_core")
                implementation("io.ktor:ktor-serialization:$ver_ktor")
                implementation("io.ktor:ktor-server-core:$ver_ktor")
                //implementation("io.ktor:ktor-server-netty:$ver_ktor")
                implementation("io.ktor:ktor-server-cio:$ver_ktor")
                implementation("ch.qos.logback:logback-classic:1.2.3")
            }
        }

        val jsMain by getting {
            dependencies {

                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core-js:$ver_kotlinx_serialization_core")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-js:$ver_kotlinx_serialization_core")
                implementation(npm("kotlinx-serialization-kotlinx-serialization-core-jslegacy", "1.4.2-RC1"))

                implementation("org.jetbrains.kotlinx:kotlinx-datetime-js:$ver_kotlinx_datetime")

                // React, React DOM + Wrappers (chapter 3)
                implementation("org.jetbrains:kotlin-react:17.0.1-pre.148-kotlin-1.4.21")
                implementation("org.jetbrains:kotlin-react-dom:17.0.1-pre.148-kotlin-1.4.21")
                implementation(npm("react", "17.0.1"))
                implementation(npm("react-dom", "17.0.1"))

                //Kotlin Styled (chapter 3)
                implementation("org.jetbrains:kotlin-styled:5.2.1-pre.148-kotlin-1.4.21")
                implementation(npm("styled-components", "~5.2.1"))

                //Video Player (chapter 7)
                implementation(npm("react-youtube-lite", "1.0.1"))

                //Share Buttons (chapter 7)
                implementation(npm("react-share", "~4.2.1"))

                //Coroutines (chapter 8)
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.3")

                //implementation("com.ccfraser.muirwik:muirwik-components:0.5.1")

                //https://github.com/CookPete/react-player
                //implementation(npm("react-player", "2.9.0"))

                // material-ui ... doesnt work
                //  implementation(npm("@material-ui/core", "4.11.4"))
                //  implementation(npm("@material-ui/icons", "4.11.2"))
                //  implementation(npm("@material-ui/pickers", "3.3.10"))
                //  implementation(npm("@material-ui/styles", "4.11.4"))
            }
        }
    }
}

val runServer by tasks.creating(JavaExec::class) {
    group = "application"
    main = "uk.co.sentinelweb.cuer.remote.server.ServerKt"
    kotlin {
        val main = targets["jvm"].compilations["main"]
        dependsOn(main.compileAllTaskName)
        dependsOn("jsBrowserDevelopmentWebpack")
        val jarTaskName = "jvmJar"
        dependsOn(jarTaskName)
        val jvmJarTask = tasks.getByName<Jar>(jarTaskName)
        classpath(
//            { main.output.allOutputs.files },
            { jvmJarTask.outputs.files },
            { configurations["jvmRuntimeClasspath"] }
        )
        println("runServer:" + main.output.allOutputs.files)
    }
    ///disable app icon on macOS
    //systemProperty("java.awt.headless", "true")
}

// include JS artifacts in any JAR we generate

tasks.getByName<Jar>("jvmJar") {
    val taskName = if (project.hasProperty("isProduction")) {
        "jsBrowserProductionWebpack"
    } else {
        "jsBrowserDevelopmentWebpack"
    }
    val webpackTask = tasks.getByName<KotlinWebpack>(taskName)
    dependsOn(webpackTask) // make sure JS gets compiled first
//    from(File(webpackTask.destinationDirectory, webpackTask.outputFileName))// bring output file along into the JAR
    from(File(webpackTask.destinationDirectory, outputJsLibName))
//    from(File(webpackTask.destinationDirectory, "cuer.js.map"))
}

tasks {
    withType<KotlinWebpack> {
        outputFileName = outputJsLibName
        //output.libraryTarget = "commonjs2"
    }
}

tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = ver_jvm
        }
    }
}