plugins {
    alias(libs.plugins.kotlin.jvm)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.io.ktor.client.cio)
    implementation(libs.io.ktor.client.core)
}
