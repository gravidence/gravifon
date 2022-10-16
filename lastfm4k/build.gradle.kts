plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

group = "org.gravidence"
version = "0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.0")

    implementation("ch.qos.logback:logback-classic:1.4.1")
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.0")
    implementation("org.http4k:http4k-core:4.30.8.0")
    implementation("org.slf4j:jcl-over-slf4j:2.0.1")
    implementation("org.slf4j:jul-to-slf4j:2.0.1")
    implementation("org.slf4j:log4j-over-slf4j:2.0.1")

    testImplementation(kotlin("test"))
    testImplementation("org.hamcrest:hamcrest:2.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.0")
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}