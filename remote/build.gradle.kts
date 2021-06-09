import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack
// ./gradlew :remote:jsBrowserRun --continue
// ./gradlew :remote:runServer

plugins {
    kotlin("multiplatform")
    application
    kotlin("plugin.serialization")
}

val ver_kotlin: String by project
val ver_kotlinx_datetime: String by project
val ver_kotlinx_serialization_core: String by project
val ver_coroutines_core: String by project
val ver_ktor: String by project
val ver_jvm: String by project
val ver_koin: String by project
val ver_kotlin_react: String by project
val ver_kotlin_styled: String by project
val ver_react: String by project
val ver_styled_cmp: String by project
val ver_jfixture: String by project
val ver_truth: String by project
val ver_logback = "1.2.3"

val outputJsLibName = "cuer.js"

group = "uk.co.sentinelweb.cuer.remote"
version = "1.0"

kotlin {
    js {
        useCommonJs()
        browser {
            commonWebpackConfig {
                cssSupport.enabled = true
            }
        }
        binaries.executable()
    }
    jvm {
    }
    sourceSets {

        val commonMain by getting {
            dependencies {
                implementation(project(":shared"))
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:$ver_kotlinx_datetime")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:$ver_kotlinx_serialization_core")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$ver_kotlinx_serialization_core")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$ver_coroutines_core")
                implementation("io.ktor:ktor-client-core:$ver_ktor")
                implementation("io.insert-koin:koin-core:$ver_koin")
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation("io.ktor:ktor-serialization:$ver_ktor")
                implementation("io.ktor:ktor-server-core:$ver_ktor")
                implementation("io.ktor:ktor-server-cio:$ver_ktor")
                implementation("ch.qos.logback:logback-classic:$ver_logback")
            }
        }

        val jvmTest by getting {
            dependencies {
                // Koin for JUnit 4
                implementation("io.insert-koin:koin-test-junit4:$ver_koin")
                implementation("com.flextrade.jfixture:jfixture:$ver_jfixture")
                implementation("com.google.truth:truth:$ver_truth")
            }
        }

        val jsMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react:$ver_kotlin_react")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom:$ver_kotlin_react")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-styled:$ver_kotlin_styled")
                implementation(npm("react-youtube-lite", "1.0.1"))
                implementation(npm("react-share", "~4.2.1"))

                implementation(project(":muirwik-components"))
                /*implementation("com.ccfraser.muirwik:muirwik-components:0.7.0") {
                    exclude(group = "org.jetbrains.kotlin-wrappers", module = "kotlin-styled")
                    exclude(group = "org.jetbrains.kotlin-wrappers", module = "kotlin-react")
                    exclude(group = "org.jetbrains.kotlin-wrappers", module = "kotlin-react-dom")
                }*/
                //https://github.com/CookPete/react-player
                //implementation(npm("react-player", "2.9.0"))
            }
            //implementation("org.jetbrains.kotlinx:kotlinx-datetime-js:$ver_kotlinx_datetime")
        }
    }
}

val runServer by tasks.creating(JavaExec::class) {
    group = "application"
    main = "uk.co.sentinelweb.cuer.remote.server.MainKt"
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
    val taskName = if (project.hasProperty("isProduction")) {
        "jsBrowserProductionWebpack"
    } else {
        "jsBrowserDevelopmentWebpack"
    }
    val webpackTask = tasks.getByName<KotlinWebpack>(taskName)
    dependsOn(webpackTask) // make sure JS gets compiled first
    from(File(webpackTask.destinationDirectory, outputJsLibName))
//    from(File(webpackTask.destinationDirectory, "cuer.js.map"))
}

tasks {
    withType<KotlinWebpack> {
        outputFileName = outputJsLibName
    }
}

tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = ver_jvm
        }
    }
}