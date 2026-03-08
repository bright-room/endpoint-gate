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
}
