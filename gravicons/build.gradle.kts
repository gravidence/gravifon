import org.jetbrains.compose.compose
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
    maven {
        url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        name = "Compose for Desktop DEV"
    }
}

dependencies {
    implementation(compose.desktop.currentOs)

    // only needed icons extracted from extended set (saves up to 30MB)
    // handy lookup page: https://fonts.google.com/icons?selected=Material+Icons&icon.style=Filled&icon.platform=android
//    implementation("org.jetbrains.compose.material:material-icons-extended-desktop:1.1.1")

    implementation("org.springframework:spring-core:5.3.23")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}