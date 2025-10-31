# Contributing to taktx-worker-examples

Thanks for wanting to contribute! This document explains the preferred workflow, how to add new example modules, and what checks to run locally before opening a pull request.

Quick checklist

- [ ] Create an issue describing the change (unless it's a tiny typo)
- [ ] Create a descriptive branch (see naming guidelines)
- [ ] Add or update a module-level `README.md` when adding a module
- [ ] Run the build and dependency checks locally
- [ ] Open a pull request with a clear description and link to the issue

Getting started

1. Fork the repository and create a branch from `main` (or the project default branch):

```bash
git checkout -b feat/my-new-example
```

Branch naming guidelines

- feature: `feat/<short-description>`
- fix: `fix/<short-description>`
- docs: `docs/<short-description>`

Module conventions

- Each example module should be a top-level directory (sibling to `client-workers-quarkus/`).
- Each module must contain its own `build.gradle` and a `README.md` documenting how to build/run that specific module.
- Prefer to keep framework-specific plugin declarations inside the module's `build.gradle`. Use the root `gradle.properties` for centrally-managed platform versions when that makes sense.

How to add a new module (step-by-step)

1. Add the module to the root `settings.gradle`:

```groovy
include 'my-new-module'
```

2. Create the folder layout:

```
my-new-module/
  build.gradle
  README.md
  src/
    main/
      java/
      resources/
```

3. Example minimal Quarkus module `build.gradle`:

```groovy
plugins {
  id 'java'
  id 'io.quarkus'
}

repositories {
  mavenLocal()
  mavenCentral()
}

dependencies {
  implementation enforcedPlatform("${quarkusPlatformGroupId}:${quarkusPlatformArtifactId}:${quarkusPlatformVersion}")
  // add framework-specific dependencies here
}
```

4. Example minimal Spring module `build.gradle`:

```groovy
plugins {
  id 'java'
}

repositories {
  mavenCentral()
}

dependencies {
  implementation 'org.springframework.boot:spring-boot-starter'
}
```

5. Add a `README.md` to the new module describing build/run commands and any special notes.

Local validation checklist (run before opening a PR)

- Run the project-level checks and build:

```bash
# quick: list projects
./gradlew projects

# full build including tests
./gradlew clean build
```

- Run the dependency updates report to detect outdated dependencies or platform BOM upgrades:

```bash
./gradlew dependencyUpdates -Drevision=release
# or the convenience task
./gradlew checkDependencyUpdates
```

- Build and run the module you're working on (example: Quarkus module):

```bash
# build module (skip tests for faster iteration)
./gradlew :client-workers-quarkus:build -x test

# run dev mode
./gradlew :client-workers-quarkus:quarkusDev
```

- Run module tests if you introduced code or changed behavior:

```bash
./gradlew :my-new-module:test
```

Code style and Java compatibility

- Java source compatibility is set in the root `build.gradle` (currently Java 21). Keep code compatible with the configured Java version.
- Keep formatting consistent with the project's conventions (use your IDE formatting or a common formatter if you add one).

Commit and PR guidelines

- Keep commits focused and well named. Use imperative present tense in commit messages (e.g., `Add new example for Spring worker`).
- Include a short description in your PR explaining why the change is needed and any manual steps to validate it.
- If you add a module, include a module-level `README.md` and make sure the module builds locally.

PR review checklist (what maintainers will look for)

- [ ] Does the change build locally (`./gradlew clean build`)?
- [ ] Are tests passing (where applicable)?
- [ ] Is the change small and focused, or if large, split into smaller PRs?
- [ ] Is module documentation included (`README.md`)?
- [ ] Are version/property changes (for example `gradle.properties`) justified and noted in the PR description?

Security and dependency concerns

- If a dependency update is required to resolve a vulnerability, prefer upgrading the platform BOM (for example Quarkus BOM in `gradle.properties`) if feasible.
- If a quick mitigation is needed, add a dependency constraint or an explicit dependency in the module's `build.gradle` to pin a safe version, and document the reason in the PR.

CI and testing notes

- The repository uses the Gradle wrapper; CI should run `./gradlew clean build`.
- If you modify or add long-running integration tests, mark them or provide a way to run them separately.

Acknowledgements

Thanks for contributing — your examples will help others learn how to integrate with TaktX and different Java frameworks.

