plugins {
    id("java-library")
    kotlin("jvm")
    kotlin("plugin.spring")
}

dependencies {
    implementation("org.mongodb:mongodb-driver-sync:3.11.2") // TODO make async
    implementation("com.github.java-json-tools:json-patch:1.11")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.10.2")

    testImplementation("org.junit.jupiter:junit-jupiter:5.6.2")
    testImplementation("io.mockk:mockk:1.9")
}