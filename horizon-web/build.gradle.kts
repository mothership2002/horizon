
plugins {
    java
}

dependencies {
    implementation(project(":horizon-core"))
    implementation("io.netty:netty-all:4.1.119.Final")
    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("ch.qos.logback:logback-classic:1.5.16")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(23))
    }
}
