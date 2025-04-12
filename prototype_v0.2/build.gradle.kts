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
        "implementation"("com.google.auto.service:auto-service-annotations:1.1.1")
        "annotationProcessor"("com.google.auto.service:auto-service:1.1.1")
    }
}