plugins {
    id("spring-boot-starter")
    id("publish-plugin")
    id("spotless-java")
}

dependencies {
    api(projects.core)
    implementation(libs.spring.boot.autoconfigure)
    implementation(libs.spring.web)
    compileOnly(projects.reactiveCore)
    compileOnly(libs.reactor.core)
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(projects.reactiveCore)
    testImplementation(libs.reactor.core)
}
