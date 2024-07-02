// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
//    val ver_kotlin: String by project
////    val ver_build_tools_gradle: String by project
//    val ver_google_services: String by project
//    val ver_navigation: String by project
//    val ver_firebase_crashlytics: String by project
//    val ver_sqldelight: String by project
//    val ver_multi_compose: String by project

    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        //maven(url = "https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }

    dependencies {
//        classpath("com.android.tools.build:gradle:${ver_build_tools_gradle}")
        classpath(libs.androidGradlePluginClaspath)
        classpath(libs.kotlinPluginClaspath)
        classpath(libs.kotlinSerializationPluginClaspath)
        classpath(libs.googleServicesPluginClaspath)
        classpath(libs.navigationPluginClaspath)
        classpath(libs.firebaseCrashlyticsPluginClaspath)
        classpath(libs.sqldelightPluginClaspath)
        classpath(libs.multiComposePluginClaspath)
//        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${ver_kotlin}")
//        classpath("org.jetbrains.kotlin:kotlin-serialization:${ver_kotlin}")
//        classpath("com.google.gms:google-services:${ver_google_services}")
//        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:${ver_navigation}")
//        classpath("com.google.firebase:firebase-crashlytics-gradle:${ver_firebase_crashlytics}")
//        classpath("com.squareup.sqldelight:gradle-plugin:${ver_sqldelight}")
//        classpath("org.jetbrains.compose:compose-gradle-plugin:${ver_multi_compose}")
    }
}

// Apply tasks to all projects
allprojects {
    tasks.create("setProductionFlag") {
        doFirst {
            val taskName = project.gradle.taskGraph.allTasks.last().toString()

            when (taskName) {
                "task ':app:assembleRelease'" -> {
                    allprojects { this.extra["isProduction"] = "true" }
                }

                "task ':assembleRelease'" -> {
                    allprojects { this.extra["isProduction"] = "true" }
                }
            }
        }
    }

    repositories {
        google()
        mavenCentral()
    }
}