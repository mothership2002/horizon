plugins {
    id("java")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":horizon-core"))

}

tasks.test {
    useJUnitPlatform()
}