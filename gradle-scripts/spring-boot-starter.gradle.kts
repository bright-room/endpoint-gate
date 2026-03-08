plugins {
    java
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

group = "net.bright-room.endpoint-gate"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// Library modules do not produce a boot jar
tasks.bootJar {
    enabled = false
}

tasks.jar {
    enabled = true
}
