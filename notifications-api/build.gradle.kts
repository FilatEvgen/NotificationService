plugins {
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.jvm)
}

group = "org.example"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.io.ktor.server.core)
    implementation(libs.io.ktor.server.netty)
    implementation(libs.io.ktor.server.content.negotiation)
    implementation(libs.io.ktor.serialization.kotlinx.json)
    implementation(libs.io.lettuce.core)
    implementation(libs.io.ktor.websockets)
    implementation(project(":common-module"))
    implementation(project(":redis-module"))
}
kotlin {
    jvmToolchain(21)
}