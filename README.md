# OSGi Liquibase

[![Build](https://github.com/BlackBeltTechnology/osgi-liquibase/actions/workflows/build.yml/badge.svg?branch=develop)](https://github.com/BlackBeltTechnology/osgi-liquibase/actions/workflows/build.yml)

## Introduction

OSGi Liquibase is an OSGi-friendly wrapper around [Liquibase](https://www.liquibase.org/) (v4.4.3), the open-source database schema migration tool. Rather than requiring consumers to manage Liquibase's classloading quirks in an OSGi container, this project bundles Liquibase itself along with helper services into a single OSGi bundle. Other bundles in the container can simply inject the `LiquibaseExecutor` service and run changelog scripts against any JDBC connection.

The project is part of the [JUDO platform](https://github.com/BlackBeltTechnology) by BlackBelt Technology.

## Architecture

The bundle exposes a small API surface (6 classes) that bridges Liquibase into OSGi:

```mermaid
classDiagram
    class LiquibaseExecutor {
        <<interface>>
        +executeLiquibaseScript(Connection, String, Bundle)
        +executeLiquibaseScript(Connection, String, Bundle, Map)
    }

    class DefaultLiquibaseExecutor {
        -scopeId: String
        -bundleContext: BundleContext
        +activate(BundleContext)
        +deactivate()
        +executeLiquibaseScript(...)
    }

    class BundleResourceAccessor {
        -bundle: Bundle
        +openStreams(String, String) InputStreamList
    }

    class StreamResourceAccessor {
        -streams: Map~String, InputStream~
        +openStreams(String, String) InputStreamList
        +list(...) SortedSet
        +describeLocations() SortedSet
    }

    class Slf4jLogService {
        -priority: int
        +getPriority() int
        +getLog(Class) Logger
    }

    class Slf4jLogger {
        -logger: Logger
        +log(Level, String, Throwable)
        +severe(String) / warning(String) / info(String) / fine(String)
    }

    LiquibaseExecutor <|.. DefaultLiquibaseExecutor : implements
    DefaultLiquibaseExecutor --> BundleResourceAccessor : creates
    DefaultLiquibaseExecutor --> Slf4jLogService : registers
    Slf4jLogService --> Slf4jLogger : creates
    ClassLoaderResourceAccessor <|-- BundleResourceAccessor : extends
    AbstractResourceAccessor <|-- StreamResourceAccessor : extends
    AbstractLogService <|-- Slf4jLogService : extends
    AbstractLogger <|-- Slf4jLogger : extends
```

### How It Works

```mermaid
sequenceDiagram
    participant Client as OSGi Client Bundle
    participant Executor as DefaultLiquibaseExecutor
    participant BRA as BundleResourceAccessor
    participant LB as Liquibase Engine
    participant DB as Database

    Client->>Executor: executeLiquibaseScript(conn, path, bundle, params)
    Note over Executor: Save & swap thread context classloader
    Executor->>BRA: new BundleResourceAccessor(bundle)
    Executor->>LB: new Liquibase(path, accessor, database)
    LB->>BRA: openStreams(path)
    BRA-->>LB: changelog XML/YAML/JSON
    LB->>DB: apply changesets via JDBC
    DB-->>LB: result
    LB-->>Executor: complete
    Note over Executor: Restore original classloader
    Executor-->>Client: done
```

### Key Design Decisions

- **Thread context classloader manipulation**: The executor saves and replaces the thread's context classloader with its own bundle classloader before every Liquibase operation. This ensures Liquibase's `ServiceLocator` and internal class scanning work correctly inside OSGi.
- **Scope lifecycle management**: Liquibase's `Scope` is entered during `@Activate` and exited during `@Deactivate`, registering the SLF4J log service and bundle classloader as scope attributes.
- **Embedded dependencies**: `liquibase-core`, `snakeyaml`, and `commons-lang3` are inlined into the bundle JAR. Liquibase's own `ServiceLocator` and `LiquibaseLogger` classes are excluded and replaced by custom implementations.
- **SPI-based logging**: `Slf4jLogService` is registered via `META-INF/services/liquibase.logging.LogService`, bridging all Liquibase log output to SLF4J.

### Dependency Graph

```mermaid
graph LR
    subgraph "Embedded in Bundle"
        LC[liquibase-core 4.4.3]
        SY[snakeyaml 1.13]
        CL[commons-lang3 3.4]
    end

    subgraph "Compile Dependencies"
        SLF4J[slf4j-api 1.7.32]
        OSGI_DS[osgi.service.component.annotations 1.3.0]
    end

    subgraph "Provided (Container)"
        OSGI[osgi.core 6.0.0]
        LB_CL[logback-classic 1.5.12]
        Lombok[lombok 1.18.34]
    end

    Bundle[osgi-liquibase] --> LC
    Bundle --> SY
    Bundle --> CL
    Bundle --> SLF4J
    Bundle --> OSGI_DS
    Bundle -.-> OSGI
    Bundle -.-> LB_CL
    Bundle -.-> Lombok
```

## Build Commands

```sh
./mvnw clean install              # Full build (compile, test, package, install)
./mvnw clean test                 # Run tests only
./mvnw clean install -DskipTests  # Build without running tests
```

> **Note:** Requires **Java 21** (Zulu JDK recommended) and **Maven 3.8.x** (wrapper included).

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for detailed instructions on submitting issues and pull requests.

## License

This project is licensed under the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0).
