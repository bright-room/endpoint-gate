import util.libs

plugins {
    id("java-conventions")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencies {
    annotationProcessor(libs.spring.boot.configuration.processor)
}

tasks {
    bootJar {
        enabled = false
    }

    jar {
        archiveClassifier.set("")
    }
}
