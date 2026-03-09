plugins {
    id("example-base")
    id("spotless-java")
}

dependencies {
    implementation(project(":spring:spring-webflux"))
    implementation(libs.spring.boot.starter.webflux)
    implementation(libs.spring.boot.starter.thymeleaf)
}
