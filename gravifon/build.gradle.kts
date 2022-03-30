import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("org.jetbrains.compose")
}

group = "org.gravidence"
version = "1.0.0"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies {
    implementation(project(":lastfm4k"))

    implementation(compose.desktop.currentOs)

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.3.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")

    // TODO think about extracting actually used icon subset only
    implementation("org.jetbrains.compose.material:material-icons-extended-desktop:1.1.1")

    implementation("ch.qos.logback:logback-classic:1.2.11")
    implementation("io.github.microutils:kotlin-logging-jvm:2.1.21")
    implementation("org.http4k:http4k-core:4.25.5.2")
    implementation("org.jsoup:jsoup:1.14.3")
    implementation("org.slf4j:jcl-over-slf4j:1.7.36")
    implementation("org.slf4j:jul-to-slf4j:1.7.36")
    implementation("org.slf4j:log4j-over-slf4j:1.7.36")

    implementation("org.springframework:spring-context:5.3.17")
    implementation("org.springframework:spring-expression:5.3.17")

    implementation("net.jthink:jaudiotagger:3.0.1")
    implementation("org.freedesktop.gstreamer:gst1-java-core:1.4.0")

    testImplementation(kotlin("test"))
    testImplementation("org.hamcrest:hamcrest:2.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
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
            packageName = "gravifon"
            packageVersion = project.version.toString()

            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
        }
    }
}