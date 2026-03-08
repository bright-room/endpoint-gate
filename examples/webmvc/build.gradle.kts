plugins {
    id("spring-boot-starter")
    id("spotless-java")
}

tasks {
    bootJar {
        enabled = true
    }

    jar {
        enabled = false
    }
}

dependencies {
    implementation(project(":spring:spring-webmvc"))
    implementation(libs.spring.boot.starter.webmvc)
}
