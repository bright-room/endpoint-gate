plugins {
    id("java-conventions")
    id("publish-plugin")
    id("spotless-java")
}

dependencies {
    implementation(libs.jspecify)
    testImplementation(libs.assertj.core)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.junit.jupiter)
}
