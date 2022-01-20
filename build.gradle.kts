import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.serialization") version "1.6.10"
    id("org.jetbrains.compose") version "1.0.1"
}

group = "org.gravidence"
version = "0.1"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies {
    implementation(compose.desktop.currentOs)

//    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.3.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")

    implementation("ch.qos.logback:logback-classic:1.2.10")
    implementation("io.github.microutils:kotlin-logging-jvm:2.1.21")
    implementation("org.slf4j:jcl-over-slf4j:1.7.33")
    implementation("org.slf4j:jul-to-slf4j:1.7.33")
    implementation("org.slf4j:log4j-over-slf4j:1.7.33")

    implementation("org.springframework:spring-context:5.3.14")

    implementation("net.jthink:jaudiotagger:3.0.1")
    implementation("org.freedesktop.gstreamer:gst1-java-core:1.4.0")

    testImplementation(kotlin("test"))
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "gravifon"
            packageVersion = "1.0.0"
        }
    }
}