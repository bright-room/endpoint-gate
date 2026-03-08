plugins {
    id("java-conventions")
    id("publish-plugin")
    id("spotless-java")
}

dependencies {
    api(projects.core)
    implementation(platform(libs.reactor.bom))
    implementation(libs.reactor.core)
    testImplementation(libs.reactor.test)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.junit.jupiter)
}
