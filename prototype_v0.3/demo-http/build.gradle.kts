plugins {
    java
    application
}

group = "horizon.demo"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":horizon-core"))
    implementation(project(":horizon-http"))


    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")
}

application {
    mainClass.set("horizon.demo.http.HttpDemoApplication")
}

tasks.test {
    useJUnitPlatform()
}
