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
    implementation("io.quarkus:quarkus-scheduler")
    implementation("io.quarkus:quarkus-arc")
    implementation(libs.quarkus.jaxb)
    implementation("io.quarkus:quarkus-smallrye-openapi")
    implementation("io.quarkus:quarkus-swagger-ui")
    implementation(libs.quarkus.resteasy.client)
    implementation("io.quarkus:quarkus-resteasy-jackson")
    implementation("io.quarkus:quarkus-jackson")
    implementation("io.quarkus:quarkus-websockets")
    implementation("io.taktx:taktx-client-quarkus:0.0.8-alpha-10")

    testImplementation(libs.quarkus.junit5)
    testImplementation(libs.quarkus.junit5.mockito)
    testImplementation("io.quarkus:quarkus-test-kafka-companion")

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
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