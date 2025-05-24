
plugins {
    java
}

dependencies {
    implementation(project(":horizon-core"))
    implementation("io.netty:netty-all:4.1.119.Final")

}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}
