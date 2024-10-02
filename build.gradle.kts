// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {

    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }

    dependencies {
        classpath(libs.androidGradlePluginClaspath)
        classpath(libs.kotlinPluginClaspath)
        classpath(libs.kotlinSerializationPluginClaspath)
        classpath(libs.googleServicesPluginClaspath)
        classpath(libs.navigationPluginClaspath)
        classpath(libs.firebaseCrashlyticsPluginClaspath)
        classpath(libs.sqldelightPluginClaspath)
        classpath(libs.multiComposePluginClaspath)
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