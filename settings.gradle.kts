pluginManagement {
    val kotlinVersion: String by settings
    val gitPropertiesPluginVersion: String by settings
    val jibPluginVersion: String by settings
    val springVersion: String by settings

    plugins {
        kotlin("jvm") version kotlinVersion
        kotlin("kapt") version kotlinVersion
        kotlin("plugin.spring") version kotlinVersion
        kotlin("plugin.allopen") version kotlinVersion

        id("org.springframework.boot") version springVersion
        id("io.spring.dependency-management") version "1.1.4"

        id("com.google.cloud.tools.jib") version jibPluginVersion
        id("com.palantir.git-version") version "0.15.0"
        id("com.gorylenko.gradle-git-properties") version gitPropertiesPluginVersion
    }
    repositories {
        mavenCentral()
        gradlePluginPortal()
        mavenLocal()
    }
}

rootProject.name = "transitclock"

include(":core")
include(":app")
