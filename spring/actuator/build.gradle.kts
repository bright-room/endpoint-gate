plugins {
    id("spring-boot-starter")
    id("publish-plugin")
    id("spotless-java")
    id("integration-test")
}

dependencies {
    api(project(":spring:spring-core"))
    implementation(projects.reactiveCore)
    implementation(libs.spring.boot.starter.actuator)
    implementation(platform(libs.reactor.bom))
    compileOnly(libs.reactor.core)
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.boot.starter.webmvc)
    testImplementation(libs.spring.boot.starter.webflux)

    integrationTestImplementation(libs.spring.boot.starter.test)
    integrationTestImplementation(libs.spring.boot.starter.webmvc)
    integrationTestImplementation(libs.spring.boot.starter.webmvc.test)
    integrationTestImplementation(libs.spring.boot.starter.webflux)
    integrationTestImplementation(libs.spring.boot.starter.webflux.test)
}
