# osgi-liquibase Specification

## Purpose

Provides an OSGi-compatible service for executing Liquibase database migration scripts, bridging Liquibase's classloading requirements with the OSGi module system and integrating Liquibase logging with SLF4J.

## Architecture

The module centers on the `LiquibaseExecutor` service interface, implemented by `DefaultLiquibaseExecutor` as an OSGi Declarative Services component. Two resource accessor implementations (`BundleResourceAccessor` and `StreamResourceAccessor`) provide flexible changelog loading. A logging bridge (`Slf4jLogService` / `Slf4jLogger`) replaces Liquibase's built-in logging with SLF4J, registered via Java SPI.

## Requirements

### Requirement: Script Execution from OSGi Bundles

The `LiquibaseExecutor` service SHALL execute Liquibase changelog scripts loaded from an OSGi `Bundle` against a given JDBC `Connection`.

#### Scenario: Execute a changelog from a bundle

- **GIVEN** a `DefaultLiquibaseExecutor` component is active in the OSGi container
- **AND** an OSGi `Bundle` contains a Liquibase changelog file at path `db/changelog.xml`
- **WHEN** `executeLiquibaseScript(connection, "db/changelog.xml", bundle)` is called
- **THEN** Liquibase SHALL parse the changelog from the bundle and apply all pending changesets to the database via the provided connection

#### Scenario: Execute a changelog with parameters

- **GIVEN** a `DefaultLiquibaseExecutor` component is active
- **AND** a map of parameters `{"schemaName": "myapp"}` is provided
- **WHEN** `executeLiquibaseScript(connection, "db/changelog.xml", bundle, parameters)` is called
- **THEN** Liquibase SHALL set each parameter as a changelog parameter before executing the update

### Requirement: Thread Context Classloader Management

The `DefaultLiquibaseExecutor` SHALL manage the thread context classloader to ensure Liquibase's internal class scanning works correctly inside OSGi.

#### Scenario: Classloader swap during execution

- **WHEN** `executeLiquibaseScript()` is invoked
- **THEN** the executor SHALL save the current thread context classloader, replace it with the executor's bundle classloader, execute Liquibase, and restore the original classloader in a `finally` block

#### Scenario: Classloader swap during activation

- **WHEN** the OSGi component is activated via `@Activate`
- **THEN** the executor SHALL swap the thread context classloader before initializing `Scope.getCurrentScope()` and entering a new Liquibase `Scope`
- **AND** SHALL restore the original classloader after scope initialization

### Requirement: Liquibase Scope Lifecycle

The `DefaultLiquibaseExecutor` SHALL manage Liquibase's `Scope` lifecycle tied to the OSGi component lifecycle.

#### Scenario: Scope entered on activation

- **WHEN** the `DefaultLiquibaseExecutor` component is activated
- **THEN** it SHALL enter a new Liquibase `Scope` with `Slf4jLogService` as the log service and the bundle classloader as the scope classloader
- **AND** SHALL store the scope ID for later exit

#### Scenario: Scope exited on deactivation

- **WHEN** the `DefaultLiquibaseExecutor` component is deactivated
- **THEN** it SHALL call `Scope.exit(scopeId)` to clean up the Liquibase scope

### Requirement: Bundle Resource Loading

The `BundleResourceAccessor` SHALL load Liquibase changelog resources from an OSGi bundle with a fallback mechanism.

#### Scenario: Load via BundleWiring classloader

- **GIVEN** a `BundleResourceAccessor` is created for a given `Bundle`
- **WHEN** `openStreams(relativeTo, streamPath)` is called
- **THEN** it SHALL first attempt to load the resource via the `BundleWiring` classloader (inherited `ClassLoaderResourceAccessor` behavior)

#### Scenario: Fallback to bundle entry

- **GIVEN** the classloader-based lookup returns no streams
- **WHEN** the parent `openStreams()` returns an empty `InputStreamList`
- **THEN** it SHALL fall back to `bundle.getEntry(streamPath)` and return the entry's input stream if found

### Requirement: In-Memory Stream Resource Loading

The `StreamResourceAccessor` SHALL load Liquibase changelog resources from an in-memory `Map<String, InputStream>`.

#### Scenario: Match stream by path

- **GIVEN** a `StreamResourceAccessor` is created with a map containing key `"db/changelog.xml"` mapped to an `InputStream`
- **WHEN** `openStreams(null, "db/changelog.xml")` is called
- **THEN** it SHALL return an `InputStreamList` containing the mapped stream

#### Scenario: Path normalization

- **GIVEN** stream keys and lookup paths may contain special characters
- **WHEN** matching is performed
- **THEN** both the key and path SHALL be normalized by replacing non-alphanumeric characters (except `-`, `_`, `.`) with underscores before comparison

### Requirement: SLF4J Logging Bridge

The `Slf4jLogService` and `Slf4jLogger` SHALL bridge all Liquibase logging output to SLF4J.

#### Scenario: Log level mapping

- **WHEN** Liquibase emits a log message
- **THEN** the logger SHALL map Liquibase levels to SLF4J as follows:

| Liquibase Level | SLF4J Level |
|----------------|-------------|
| FINEST | trace |
| FINE, DEBUG | debug |
| CONFIG, INFO | info |
| WARNING | warn |
| SEVERE | error |

#### Scenario: SPI registration

- **GIVEN** the bundle JAR contains `META-INF/services/liquibase.logging.LogService` pointing to `Slf4jLogService`
- **WHEN** Liquibase's `StandardServiceLocator` scans for `LogService` implementations
- **THEN** `Slf4jLogService` SHALL be discovered and used (default priority: 5)

#### Scenario: Configurable priority

- **GIVEN** the system property `hu.blackbelt.osgi.liquibase.logging.Slf4jLogService.priority` is set to `10`
- **WHEN** `Slf4jLogService` is instantiated
- **THEN** `getPriority()` SHALL return `10` instead of the default `5`
