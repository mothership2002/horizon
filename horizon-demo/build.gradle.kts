
plugins {
    java
    application
}

application {
    mainClass.set("horizon.HorizonApplicationDemo")
}

dependencies {
    implementation(project(":horizon-core"))
    implementation(project(":horizon-web"))
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(23))
    }
}
