plugins {
    id("java")
    alias(libs.plugins.quarkus)
    alias(libs.plugins.spotless)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks {
    withType<JavaCompile>().configureEach {
        options.release = 21
    }

    withType<Javadoc>().configureEach {
        with(options as StandardJavadocDocletOptions) {
            addStringOption("-release", "21")
        }
    }

    withType<Test>().configureEach {
        javaLauncher.set(project.javaToolchains.launcherFor {
            languageVersion = JavaLanguageVersion.of(21)
        })
    }
}

dependencies {
    implementation(enforcedPlatform(libs.quarkus.camel.bom.get()))
    implementation(enforcedPlatform(libs.quarkus.bom.get()))
    implementation(libs.quarkus.rest)
    implementation(libs.quarkus.arc)
    implementation(libs.quarkus.jaxb)
    implementation(libs.taktx.client.quarkus)
    testImplementation(libs.quarkus.junit5)
}

// Adds dependency locking to ensure reproducible builds
dependencyLocking {
    lockAllConfigurations()
}
