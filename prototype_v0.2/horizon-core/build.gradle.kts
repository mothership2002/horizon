
plugins {
    java
}

dependencies {
    implementation("org.reflections:reflections:0.10.2")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(23))
    }
}
