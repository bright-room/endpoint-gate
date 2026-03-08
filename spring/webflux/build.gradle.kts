plugins {
    id("spring-boot-starter")
    id("publish-plugin")
    id("spotless-java")
    id("integration-test")
}

description = "endpoint-gate spring-webflux module"

dependencies {
    api(projects.springCore)
    api(projects.reactiveCore)
    implementation(libs.spring.boot.starter.webflux)
    implementation(libs.spring.boot.starter.aspectj)
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.reactor.test)
}
