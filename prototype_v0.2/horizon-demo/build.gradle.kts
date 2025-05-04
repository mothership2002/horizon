plugins {
    id("java")
}

group = "demo"
version = "23"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":horizon-core"))
    implementation(project(":horizon-web"))
    implementation(project(":horizon-http"))
}

tasks.test {
    useJUnitPlatform()
}