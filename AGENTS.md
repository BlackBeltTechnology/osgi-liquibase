# OSGi Liquibase - Project Documentation

## Project Overview


**Repository:** BlackBeltTechnology/osgi-liquibase
**License:** Apache License 2.0
**Java Version:** 21 (Azul Zulu JDK)
**Build System:** Maven 3.8.x with Maven Wrapper (`./mvnw`)

1. Wraps [Liquibase 4.4.3](https://www.liquibase.org/) into a self-contained OSGi bundle, embedding `liquibase-core`, `snakeyaml`, and `commons-lang3`
2. Provides `LiquibaseExecutor`, an OSGi Declarative Services component that other bundles can inject to run database migration scripts
3. Includes custom resource accessors (`BundleResourceAccessor`, `StreamResourceAccessor`) that allow loading Liquibase changelogs from OSGi bundles or in-memory streams
4. Bridges Liquibase logging to SLF4J via a custom `LogService` registered through Java SPI
5. Manages thread context classloader and Liquibase `Scope` lifecycle to ensure correct operation inside OSGi containers

## Code Instructions

1. First think through the problem, read the codebase for relevant files.
2. Before you make any major changes, check in with me and I will verify the plan.
3. Please every step of the way just give me a high level explanation of what changes you made.
4. Make every task and code change you do as simple as possible. We want to avoid making any massive or complex changes. Every change should impact as little code as possible. Everything is about simplicity.
5. Maintain a documentation file that describes how the architecture of the app works inside and out.
6. Never speculate about code you have not opened. If the user references a specific file, you MUST read the file before answering. Make sure to investigate and read relevant files BEFORE answering questions about the codebase. Never make any claims about code before investigating unless you are certain of the correct answer - give grounded and hallucination-free answers.
7. For implementation use TDD (Test-Driven Development): write or update tests first to define the expected behaviour, verify they fail, then write the minimal implementation to make them pass.
8. Use DRY (Don't Repeat Yourself): extract reusable logic into separate classes, utilities, or components. If the same pattern appears in multiple places, refactor it into a shared helper.

## Directory Structure

```
osgi-liquibase/
├── src/main/java/hu/blackbelt/osgi/liquibase/   # All source code (6 classes)
│   ├── impl/                                      # Service implementation
│   └── logging/                                   # SLF4J logging bridge
├── src/main/resources/META-INF/services/          # Java SPI registration
├── .github/workflows/                             # CI/CD pipeline (GitHub Actions)
├── .mvn/                                          # Maven wrapper config & JVM settings
├── pom.xml                                        # Build configuration
└── logback-test.xml                               # Test logging config
```

## Core Modules

This is a single-module project (no `<modules>` in pom.xml). All code lives under `src/main/java/hu/blackbelt/osgi/liquibase/`.

| Package | Type | Purpose |
|---------|------|---------|
| `hu.blackbelt.osgi.liquibase` | API + Accessors | `LiquibaseExecutor` service interface and two resource accessor implementations for loading changelog files |
| `hu.blackbelt.osgi.liquibase.impl` | Implementation | `DefaultLiquibaseExecutor` — OSGi DS component that manages Liquibase Scope lifecycle and executes migration scripts |
| `hu.blackbelt.osgi.liquibase.logging` | Logging Bridge | `Slf4jLogService` + `Slf4jLogger` — bridges Liquibase's logging API to SLF4J, registered via `META-INF/services` |

### Key Classes

| Class | Role |
|-------|------|
| `LiquibaseExecutor` | Public service interface with two `executeLiquibaseScript()` overloads |
| `DefaultLiquibaseExecutor` | `@Component(immediate=true)` — enters Liquibase Scope on activate, swaps thread classloader for each execution |
| `BundleResourceAccessor` | Extends `ClassLoaderResourceAccessor`, loads changelogs from an OSGi `Bundle` via `BundleWiring` classloader with fallback to `bundle.getEntry()` |
| `StreamResourceAccessor` | Extends `AbstractResourceAccessor`, loads changelogs from an in-memory `Map<String, InputStream>` |
| `Slf4jLogService` | Implements Liquibase `LogService` (priority 5, configurable via system property), creates `Slf4jLogger` instances |
| `Slf4jLogger` | Maps Liquibase log levels (FINEST→trace, FINE→debug, INFO→info, WARNING→warn, SEVERE→error) to SLF4J |

## Technology Stack

### Core Technologies
- **Liquibase** 4.4.3 — database schema migration engine (embedded in bundle)
- **OSGi Core** 6.0.0 — module system framework
- **OSGi Declarative Services** 1.3.0 — component model annotations
- **SLF4J** 1.7.32 — logging facade
- **Lombok** 1.18.34 — compile-time code generation (`@Slf4j`, `@RequiredArgsConstructor`, `@NonNull`)
- **SnakeYAML** 1.13 — YAML parsing for Liquibase (embedded)
- **Commons Lang** 3.4 — utility library (embedded)

### Build & Quality
- **Maven** 3.8.x with Maven Wrapper
- **Apache Felix Maven Bundle Plugin** 6.0.0 — generates OSGi bundle with embedded dependencies
- **JaCoCo** 0.8.12 — code coverage
- **SonarQube** — static analysis (develop branch)
- **Maven Surefire** 3.5.1 — test execution with Java 21 module-system `--add-opens`

## Build Commands

```sh
./mvnw clean install              # Full build
./mvnw clean test                 # Run tests only
./mvnw clean install -DskipTests  # Skip tests
./mvnw clean verify               # Build + quality checks (JaCoCo)
```

> **Note:** The Maven Wrapper (`./mvnw`) is included — no local Maven installation required.

### Maven Profiles

| Profile | Purpose |
|---------|---------|
| `sign-artifacts` | GPG-sign artifacts using `sign-maven-plugin` |
| `release-dummy` | Deploy to local `/tmp/` directory (testing) |
| `release-judong` | Deploy to JUDO Nexus (`nexus.judo.technology`) |
| `release-central` | Deploy to Maven Central via OSSRH (`oss.sonatype.org`) |
| `generate-github-asciidoc-diagrams` | Generate PlantUML diagrams from AsciiDoc sources |
| `update-source-code-license` | Update Apache 2.0 license headers in source files |

## Key Configuration Files

| File | Purpose |
|------|---------|
| `pom.xml` | Single-module Maven build with OSGi bundle packaging, dependency embedding, and 6 profiles |
| `.mvn/extensions.xml` | Maven Wagon extensions for file/WebDAV deployment |
| `.mvn/jvm.config` | JVM heap settings (`-Xms1024m -Xmx2048m`) for Maven builds |
| `logback-test.xml` | SLF4J/Logback console appender for test execution |
| `src/main/resources/META-INF/services/liquibase.logging.LogService` | Java SPI registration for `Slf4jLogService` |
| `.github/workflows/build.yml` | Primary CI workflow (build, deploy, tag, release) |
| `.github/workflows/release.yml` | Manual release trigger workflow |

## Development Environment

**Required:**
- Java 21 JDK ([Azul Zulu](https://www.azul.com/downloads/?version=java-21-lts&package=jdk) recommended)
- Git

**Optional:**
- Maven 3.8.x+ (wrapper `./mvnw` is included)
- IDE with Lombok support (IntelliJ IDEA, VS Code with Lombok extension)

## Git Workflow

- **Main Branch:** `develop`
- **Versioning:** `1.3.0` (CI-friendly via `${revision}` property)
- **Branching model:** GitFlow — `develop`, `feature/*`, `bugfix/*`, `release/*`, `hotfix/*`, `master`
- **Commit rule:** Every commit must reference a JIRA ticket (`JNG-xxx`)
- **Branch naming:** `feature/JNG-NUMBER_summary`, `bugfix/JNG-NUMBER_summary`
- **CI/CD:** GitHub Actions with automated deployment to JUDO Nexus and Maven Central

## Important Notes

1. **Classloader management is critical** — `DefaultLiquibaseExecutor` swaps the thread context classloader before every Liquibase call and restores it in a `finally` block. This pattern must be preserved for Liquibase to function in OSGi.
2. **Liquibase's own ServiceLocator and LiquibaseLogger are excluded** from the embedded JAR — they are replaced by the project's SPI-based logging bridge.
3. **All OSGi imports use `resolution:=optional`** — the bundle is highly portable but may silently miss dependencies at runtime.
4. **No tests currently exist** — `src/test/` directory is empty.
5. **Lombok is used for code generation** — `@Slf4j`, `@RequiredArgsConstructor`, `@NonNull` annotations. Do not expand Lombok-generated code manually.
6. **The `Slf4jLogService` priority is configurable** via system property `hu.blackbelt.osgi.liquibase.logging.Slf4jLogService.priority` (default: 5).

## Related Documentation

- [README.md](README.md) — Project introduction and architecture overview
- [CONTRIBUTING.md](CONTRIBUTING.md) — Development setup and contribution guidelines
- [CI Flow Documentation](.github/CIFLOW.md) — Detailed CI/CD pipeline and branching strategy
