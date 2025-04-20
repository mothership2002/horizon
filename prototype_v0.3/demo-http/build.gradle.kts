
plugins {
    java
    application
}


dependencies {
    implementation(project(":horizon-http"))
    implementation(project(":horizon-core"))
    implementation(project(":horizon-ws"))

}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(23))
    }
}


tasks.test {
    useJUnitPlatform()
}