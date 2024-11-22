plugins {
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.jvm)
    id("com.google.devtools.ksp") version "2.0.21-1.0.26"
    id("eu.vendeli.telegram-bot") version "7.4.1"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.io.ktor.server.core)
    implementation(libs.io.ktor.server.netty)
    implementation(libs.io.ktor.server.content.negotiation)
    implementation(libs.io.ktor.serialization.kotlinx.json)
    implementation(libs.io.lettuce.core)
    implementation(libs.io.ktor.server.websockets)
    implementation(libs.io.ktor.client.cio)
    implementation(libs.io.ktor.client.core)
    implementation(libs.io.ktor.client.logging)
    implementation("eu.vendeli:telegram-bot:7.4.1")
    ksp("eu.vendeli:ksp:7.4.1")
    implementation(project(":common-module"))
    implementation(project(":redis-module"))
}
kotlin {
    jvmToolchain(21)
}