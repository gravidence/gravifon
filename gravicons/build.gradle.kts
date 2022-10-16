import org.jetbrains.compose.jetbrainsCompose
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
}

group = "org.gravidence"
version = "0.1"

repositories {
    google()
    mavenCentral()
    jetbrainsCompose()
}

dependencies {
    implementation(compose.desktop.currentOs)

    // only needed icons extracted from extended set (saves up to 30MB)
    // handy lookup page: https://fonts.google.com/icons?selected=Material+Icons&icon.style=Filled&icon.platform=android
//    api(compose.materialIconsExtended)

    implementation("org.springframework:spring-core:5.3.23")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}