
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
    implementation(project(":horizon-app"))
    implementation(project(":horizon-data"))
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(23))
    }
}
