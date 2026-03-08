plugins {
    id("spring-boot-starter")
    id("publish-plugin")
    id("spotless-java")
    id("integration-test")
}

dependencies {
    api(project(":spring:spring-core"))
    implementation(libs.spring.boot.starter.webmvc)
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.boot.starter.webmvc.test)
    testImplementation(libs.jsoup)
    integrationTestImplementation(libs.spring.boot.starter.thymeleaf)
}
