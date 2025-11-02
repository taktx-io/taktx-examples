plugins {
    alias(libs.plugins.java)
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
    implementation(libs.quarkus.scheduler)
    implementation(libs.quarkus.arc)
    implementation(libs.quarkus.jaxb)
    implementation(libs.quarkus.smallrye.openapi)
    implementation(libs.quarkus.swagger.ui)
    implementation(libs.quarkus.resteasy.client)
    implementation(libs.quarkus.resteasy.jackson)
    implementation(libs.quarkus.jackson)
    implementation(libs.quarkus.websockets)
    implementation(libs.taktx.client.quarkus)

    testImplementation(libs.quarkus.junit5)
    testImplementation(libs.quarkus.junit5.mockito)
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