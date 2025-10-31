plugins {
    id("java")
    id("io.quarkus")
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
    implementation("io.quarkus:quarkus-rest")
    implementation("io.quarkus:quarkus-arc")
    implementation("io.quarkus:quarkus-jaxb")
    implementation("io.taktx:taktx-client-quarkus:0.0.8-alpha-10")

    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.rest-assured:rest-assured")
}

// Adds dependency locking to ensure reproducible builds
dependencyLocking {
    lockAllConfigurations()
}

spotless {
    java {
        googleJavaFormat()
    }
}