plugins {
    id("com.diffplug.spotless")
}

spotless {
    java {
        googleJavaFormat("1.25.2")
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
    }
    kotlinGradle {
        target("**/*.gradle.kts")
        ktlint()
        trimTrailingWhitespace()
        endWithNewline()
    }
}
