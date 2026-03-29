import util.libs

plugins {
    `java-library`
}

group = "net.bright-room.endpoint-gate"
version = providers.gradleProperty("releaseVersion").getOrElse("0.0.0-SNAPSHOT")

dependencies {
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

java {
    withJavadocJar()
    withSourcesJar()
    toolchain {
        languageVersion = JavaLanguageVersion.of(libs.versions.java.get())
    }
}

tasks {
    test {
        useJUnitPlatform()
        maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)
    }

    javadoc {
        (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:all", "-quiet")
        isFailOnError = true
    }
}
