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
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}