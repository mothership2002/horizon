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
}

tasks.test {
    useJUnitPlatform()
}