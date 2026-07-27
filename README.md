<h1 align="center">nscale Query Tester</h1>

<p align="center">
  An IntelliJ IDEA plugin for running <b>NQL</b> queries against the
  <a href="https://www.ceyoniq.com/">Ceyoniq</a> <i>nscale Application Layer</i> — without leaving your IDE.
</p>

<p align="center">
  <img alt="Version" src="https://img.shields.io/jetbrains/plugin/v/19985-nscale-query-tester">
  <img alt="Downloads" src="https://img.shields.io/jetbrains/plugin/d/19985-nscale-query-tester">
  <img alt="Rating" src="https://img.shields.io/jetbrains/plugin/r/rating/19985-nscale-query-tester">
  <img alt="License" src="https://img.shields.io/badge/license-MIT-blue">
</p>

<p align="center">
  <a href="#installation">Installation</a> ·
  <a href="#getting-started">Getting started</a> ·
  <a href="#features">Features</a> ·
  <a href="#settings">Settings</a> ·
  <a href="#building-from-source">Building from source</a>
</p>

![screenshot][5]

---

## What is it?

The nscale Application Layer is the core component of Ceyoniq's enterprise information management system. Testing a
query against it usually means switching tools, retyping connection data and squinting at raw output.

**nscale Query Tester** puts that loop into a tool window at the bottom of your IDE: pick a connection, type an NQL
query with full completion support, hit <kbd>Ctrl</kbd>+<kbd>Enter</kbd>, and browse the result as a paged table —
including basic repository operations such as creating, locking and deleting resources straight from the result row.

## Features

### Query

- **Multiple query tabs** — every tab is an independent query context with its own connection, mode and result set.
- **Query modes** — *Repository*, *Masterdata* and *Principals*, each with its own options panel.
- **Query variants** — `Aggregate` and `Version` for repository queries, `Aggregate` for principals.
- **Scoped repository search** — set a root resource id and search only below that folder, or jump into a folder from
  the result table via *Search From Here* / *Search Parent*.
- **NQL completion** — keywords, named operators, query functions, `$`-constants and context variables, each shown with
  its category. Functions are inserted with parentheses and the caret placed inside. On top of that, the indexing
  property names of the selected document area are fetched live from the session and offered as completions.
- **Query history** — the last queries are kept per project and are one dropdown away.

### Results

- **Dynamic result table** — columns follow the result table returned by the server, no fixed schema.
- **Paging** — first / previous / next / last with a page counter; the page size is the configured max result size.
- **Lock indicator** — a padlock column marks locked resources and updates immediately when you lock or unlock a row.
- **Clipboard helpers** — *Copy Key* for the resource id, *Copy Selected Values* for the whole row with a configurable
  field separator.

### Repository operations

Right-click any result row:

| Action                           | Description                                                     |
|----------------------------------|-----------------------------------------------------------------|
| Search From Here / Search Parent | Re-run the query rooted at the selected folder or its parent    |
| Show Path                        | Show the full repository path and optionally navigate to a node |
| Lock / Unlock                    | Lock or unlock the selected resource                            |
| Add Document / Add Folder        | Create a new resource below the selected folder                 |
| Delete Document/Folder           | Delete the selected resource (must be enabled in the settings)  |

## Requirements

- IntelliJ IDEA **2024.3** (build 243)
- A reachable nscale Application Layer instance and valid credentials

## Installation

### From the JetBrains Marketplace

<kbd>Settings</kbd> → <kbd>Plugins</kbd> → <kbd>Marketplace</kbd> → search for *nscale Query Tester* → <kbd>
Install</kbd>, or grab it directly from the [plugin page][1].

### From disk

Download the latest release [from GitHub][3] and follow the JetBrains guide on
[installing a plugin from disk][2].

## Getting started

1. Open the **QueryTester** tool window at the bottom of the IDE.
2. Click **Add Connection** in the toolbar and enter server, port, instance, credentials and (optionally) SSL and
   timeouts. Connections are stored application-wide, so they are available in every project.
3. Select the connection and a **document area** in the settings panel.
4. Choose a query mode tab — *Repository*, *Masterdata* or *Principals* — and set its options.
5. Type your NQL query and press <kbd>Ctrl</kbd>+<kbd>Enter</kbd> (or hit **Execute Active Query** in the toolbar).

> Paging is added automatically: the plugin injects the `paging(...)` clause and the result count into the query, so
> you write plain NQL and still get a paged table back.

## Settings

<kbd>Settings</kbd> → <kbd>Tools</kbd> → <kbd>QueryTester</kbd>, or the gear icon in the tool window toolbar:

| Setting           | Default                 | Description                                         |
|-------------------|-------------------------|-----------------------------------------------------|
| Max result size   | `100`                   | Rows per page                                       |
| Max history size  | `25`                    | Number of remembered queries                        |
| Show id column    | on                      | Show the synthetic line number column               |
| Show key column   | on                      | Show the resource key column                        |
| Display lock icon | on                      | Show the padlock column for locked resources        |
| Show delete       | off                     | Enable *Delete Document/Folder* in the context menu |
| Font face / size  | `JetBrains Mono` / `14` | Font of the result table                            |
| Field separator   | <code>&#124;</code>     | Separator used by *Copy Selected Values*            |

## Building from source

```bash
./gradlew build          # compile + assemble
./gradlew buildPlugin    # distributable zip in build/distributions/
./gradlew runIde         # sandbox IDE with the plugin loaded
./gradlew verifyPlugin   # JetBrains plugin verifier
```

The nscale connector (`com.ceyoniq.nscale.applicationlayer:al-advanced-connector`) comes from a private Maven
repository. Without credentials the build cannot resolve its dependencies. Add them to `~/.gradle/gradle.properties`:

```properties
ceyoniq_artifact_username = <your-user>
ceyoniq_artifact_password = <your-password>
```

Requires JDK 21. Version knobs (`pluginVersion`, `ideaVersion`, …) live in `gradle.properties`.

## Caveats

The plugin is fully operational, but it is developed alongside day-to-day work and may still contain bugs.
`BPNM` and `Workflow` exist in the query model but are not implemented yet.

## Contributing

This project is under active development. Bug reports, feedback and suggestions are very welcome — please open an entry
in the [issue list][4]. Pull requests are just as welcome; note that there is no test suite, so please verify your
change via `./gradlew runIde` against a real Application Layer and describe what you tested.

## License

Released under the [MIT license](LICENSE).

[1]: https://plugins.jetbrains.com/plugin/19985-nscale-query-tester

[2]: https://www.jetbrains.com/help/idea/managing-plugins.html#install_plugin_from_disk

[3]: https://github.com/homburgs/querytester/releases

[4]: https://github.com/homburgs/querytester/issues

[5]: https://github.com/homburgs/querytester/blob/develop/docs/screenshot_small.png?raw=true
