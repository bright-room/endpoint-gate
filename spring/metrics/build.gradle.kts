plugins {
    id("spring-boot-starter")
    id("publish-plugin")
    id("spotless-java")
    id("integration-test")
}

dependencies {
    api(project(":spring:spring-core"))
    implementation(projects.reactiveCore)
    implementation(libs.spring.boot.autoconfigure)
    implementation(libs.spring.web)
    implementation(libs.micrometer.core)
    implementation(platform(libs.reactor.bom))
    compileOnly(libs.reactor.core)

    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.micrometer.test)
    testImplementation(platform(libs.reactor.bom))
    testImplementation(libs.reactor.test)

    integrationTestImplementation(project(":spring:spring-webmvc"))
    integrationTestImplementation(project(":spring:spring-webflux"))
    integrationTestImplementation(libs.spring.boot.starter.test)
    integrationTestImplementation(libs.spring.boot.starter.webmvc)
    integrationTestImplementation(libs.spring.boot.starter.webmvc.test)
    integrationTestImplementation(libs.spring.boot.starter.webflux)
    integrationTestImplementation(libs.spring.boot.starter.webflux.test)
    integrationTestImplementation(libs.micrometer.test)
}
