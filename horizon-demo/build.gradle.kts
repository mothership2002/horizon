
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

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(23))
    }
}
