plugins {
    `maven-publish`
    signing
}

java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            pom {
                name = provider { "${project.group}:${project.name}" }
                description = provider { project.description ?: project.name }
                url = "https://github.com/bright-room/endpoint-gate"
                licenses {
                    license {
                        name = "The Apache License, Version 2.0"
                        url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                    }
                }
                developers {
                    developer {
                        id = "bright-room"
                        name = "bright-room"
                        url = "https://github.com/bright-room"
                    }
                }
                scm {
                    connection = "scm:git:git://github.com/bright-room/endpoint-gate.git"
                    developerConnection = "scm:git:ssh://github.com/bright-room/endpoint-gate.git"
                    url = "https://github.com/bright-room/endpoint-gate"
                }
                issueManagement {
                    system = "GitHub Issues"
                    url = "https://github.com/bright-room/endpoint-gate/issues"
                }
            }
        }
    }
}

signing {
    sign(publishing.publications["mavenJava"])
}
