# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

QueryTester is an IntelliJ IDEA plugin ("nscale Query Tester") that provides a tool window for connecting to the
**nscale Application Layer** (Ceyoniq Technology) and running NQL (nscale Query Language) queries against repository,
masterdata, and principal data. Results render in an editable table. Pure Java (no Kotlin), Swing UI, built on the
IntelliJ Platform Gradle plugin 2.x.

## Build & Run

```bash
./gradlew build                 # compile + assemble the plugin
./gradlew buildPlugin           # produce the distributable zip in build/distributions/
./gradlew runIde                # launch a sandbox IDE with the plugin loaded
./gradlew verifyPlugin          # run the JetBrains plugin verifier (IDEA Community 2024.3 + recommended IDEs)
./gradlew publishPlugin         # publish to the JetBrains Marketplace (needs token)
```

There are no tests in this repo (`src/test` does not exist), despite the JUnit test dependencies declared in
`build.gradle`. Verify changes via `runIde`.

Key version knobs live in `gradle.properties`: `pluginVersion`, `ideaVersion`, `javaSourceVersion`/
`javaTargetVersion` (Java 21). `sinceBuild` is `243`, there is no `until-build` (open-ended; see `build.gradle`
and `plugin.xml`).

### Required credentials

The nscale connector (`com.ceyoniq.nscale.applicationlayer:al-advanced-connector`) is pulled from a private Maven repo.
`build.gradle` reads these Gradle properties, which must be set (e.g. in
`~/.gradle/gradle.properties`), or the build cannot resolve dependencies:

- `ceyoniq_artifact_username`, `ceyoniq_artifact_password` — nscale artifact repo auth
- `jetbrains_marketplace_token_querytester` — only needed for publishing

## Architecture

All code is under `com.hsofttec.intellij.querytester`. The UI is Swing assembled in Java (a few
`.form` files exist for dialogs), not declarative.

### Entry point and UI tree

- `QueryTesterToolWindowFactory` — registered in `plugin.xml` as a bottom tool window; creates the root `QueryTester`
  panel.
- `QueryTester` (`SimpleToolWindowPanel`) — owns the toolbar and a `QueryTabbedPane`. This is the central hub: it
  subscribes to all message-bus topics and translates UI state into a
  `NscaleQueryInformation` before dispatching query execution.
- `QueryTabbedPane` → `QueryTab` — each tab is an independent query context. A `QueryTab` wires together four
  sub-panels: `QueryTextboxPanel` (NQL editor), `QuerySettingsPanel` (connection + history), `QueryOptionsTabbedPane`
  (Repository / Masterdata / Principals / BPNM / Workflow modes and their options), and `QueryResultTablePanel` (the
  result `NscaleTable`).

### Message bus (decoupling mechanism)

Components do **not** call each other directly. They communicate through IntelliJ's project
`MessageBus` using the notifier interfaces in `ui/notifiers/` (each defines a `Topic`). Typical flow: a UI action
publishes `PrepareQueryExecutionNotifier` → `QueryTester` gathers state and publishes `StartQueryExecutionNotifier` →
the active `QueryTab` runs the query. Other topics:
`CheckServerConnectionNotifier`, `ConnectionsModifiedNotifier`, `DocumentAreaChangedNotifier`,
`RootResourceIdChangedNotifier`, `FontSettingsChangedNotifier`. When adding cross-component behavior, prefer
adding/using a notifier over direct references.

### Services

- `ConnectionService` — **singleton** (not an IntelliJ service; `getInstance()`), owns the single nscale `Session`. All
  calls into the nscale SDK (login, lock/unlock, create folder/document, fetch resources, objectclass config) go through
  here. Note the thread context-classloader swap in
  `createConnection()` — the connector needs the plugin classloader.
- `QueryService` — project service; executes the actual NQL search per `QueryMode`/`QueryType` and converts nscale
  `ResultTable`s into `commons-beanutils` `DynaBean`/`BasicDynaClass` rows wrapped in `NscaleResult`. `prepareQuery()`
  auto-injects `paging(...)` and `count` into the NQL.
- `HistoryService` — project service, `@State`-persisted to `querytester.xml`; stores the query history list and
  notifies `HistoryModifiedEventListener`s.
- `SettingsService` — application service, persisted to `querytester-settings.xml`; global settings (`SettingsState`:
  max result size, history size, fonts, column visibility).
- `ConnectionSettingsService` — application service; stores the list of configured connections.

Persisted state classes live in `states/` (`SettingsState`, `ConnectionSettings`,
`ConnectionSettingsState`) and implement the `PersistentStateComponent` pattern.

### Query model

`NscaleQueryInformation` (in `models/`) is the value object carrying everything needed to run one query: connection,
document area, `QueryMode` (REPOSITORY / MASTERDATA / PRINCIPALS / BPNM / WORKFLOW), `QueryType` (DEFAULT / AGGREGATE /
VERSION / AGGREGATE_AND_VERSION), NQL text, root resource, masterdata scope. `QueryService.proccessQuery` branches on
mode; only REPOSITORY, MASTERDATA, and PRINCIPALS are implemented.

Result rows are dynamic: columns come from the nscale `ResultTable` at runtime, so rows are
`DynaBean`s and the table uses `DynaClassTableModel`. Fixed synthetic columns (line no, key, locked) are defined via
`QueryTesterConstants.DBEAN_PROPERTY_NAME_*`.

### NQL language support

`NqlLanguage` / `NqlFileType` register a lightweight language; `NqlCompletionProvider`
(a `TextFieldCompletionProviderDumbAware`) supplies completions from `NqlLiterals.LITERALS` plus the indexing property
names fetched live from the nscale session when the document area changes.

## Conventions

- Lombok is used (`@Getter`/`@Setter`); it is `compileOnly` + `annotationProcessor`.
- The MIT license header block is present at the top of every source file — keep it when creating new files.
- `CHANGELOG.md` follows Keep-a-Changelog; the `org.jetbrains.changelog` plugin reads it and injects the latest section
  into the plugin's change-notes at build time. Add entries under `[Unreleased]`.
