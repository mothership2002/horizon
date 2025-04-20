plugins {
    id("java")
}

group = "com.simple"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    // Netty dependency moved to horizon-http and horizon-ws modules
}

tasks.test {
    useJUnitPlatform()
}
