plugins {
    id("java")
}

group = "com.simple"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":horizon-core"))
    implementation(project(":horizon-web"))
    implementation("io.netty:netty-all:4.1.119.Final")
    implementation("com.google.auto.service:auto-service-annotations:1.1.1")
    annotationProcessor("com.google.auto.service:auto-service:1.1.1")
}

tasks.test {
    useJUnitPlatform()
}