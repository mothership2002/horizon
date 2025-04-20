plugins { base }

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")

    dependencies {
        "implementation"("org.slf4j:slf4j-api:2.0.9")
        "implementation"("ch.qos.logback:logback-classic:1.5.16")

        "testImplementation"(platform("org.junit:junit-bom:5.10.0"))
        "testImplementation"("org.junit.jupiter:junit-jupiter")
    }
}