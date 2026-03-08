plugins {
    id("spring-boot-starter")
    id("publish-plugin")
    id("spotless-java")
    id("integration-test")
}

dependencies {
    api(project(":spring:spring-core"))
    api(projects.reactiveCore)
    implementation(libs.spring.boot.starter.webflux)
    implementation(libs.spring.boot.starter.aspectj)
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.boot.starter.webflux.test)
    testImplementation(libs.jsoup)
}
