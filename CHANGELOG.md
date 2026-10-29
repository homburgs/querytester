# Changelog

## [Unreleased]

### Added

### Changed

### Removed

### Fixed

### Security

## [1.5.1] - 2026-10-29

### Added

- Edit result cells inline by double click (repository and masterdata results); the value is saved to the server
  after confirmation.
- Columns lineno and key stay fixed when scrolling the result table horizontally (setting, enabled by default).

### Changed

- Compatible with IntelliJ IDEA 2024.3 up to 2026.2 (`until-build` 262.*).
- Updated dependencies: commons-lang3 3.20.0, commons-collections4 4.5.0, Lombok 1.18.48; build against IntelliJ IDEA
  Community 2024.3.6.

### Removed

- Unused dependency on the bundled Java plugin.
- Unused dependency LGoodDatePicker.

### Fixed

- Setting "show key column" now hides the key column for all queries, not only for repository aggregate queries.
- Hiding the id, key or lock column no longer hides data columns of principal queries.

## 1.5.0 - 2026-10-21

### Added

- NQL autocompletion now covers the full language surface from the NQL reference: query functions, aggregate keywords,
  named operators, and convenience `$`-constants. Completions show a category (type text + icon), and functions insert
  with parentheses and the caret placed inside.
- The result table supports multi selection (Ctrl/Shift). Lock, Unlock and Delete from the context menu apply to all
  selected resources; the other context menu items stay single selection only.
- Selected resources can be deleted with the Delete key. Deleting now asks
  for confirmation, where the user chooses between logical (restorable) and physical deletion.
- Masterdata entries can be deleted (physically only) from the result table in masterdata mode, via context menu or
  Delete key, for single and multi selection. The context menu item is now labelled "Delete".

### Changed

- Compatible with IntelliJ IDEA 2024.3 and later (no upper build limit).

### Removed

- The setting "enable Delete" was removed. Delete is always available in repository and masterdata mode and is guarded
  by a confirmation dialog.

### Fixed

- Locking or unlocking a resource from the result table context menu now updates the lock column immediately; previously
  the padlock icon only changed after the query was re-run.
- The lock indicator column now shows a padlock icon for locked resources. Previously the locked column rendered blank
  (the column was routed to the platform's built-in icon renderer, which received the row bean instead of an icon), and
  unlocked rows showed the literal text `false`.

## 1.4.5 - 2025-03-20

### Changed

- upgrade for IDEA 2024.3

## 1.4.4 - 2024-08-27

### Changed

- upgrade for IDEA 2024.2

## 1.4.3 - 2024-04-05

### Changed

- upgrade for IDEA 2024.1

## 1.4.2 - 2023-12-10

### Changed

- upgrade for IDEA 2023.3

## 1.4.1 - 2023-08-14

### Changed

- upgrade for IDEA 2023.2

## 1.4.0 - 2023-04-03

### Added

- principals now queryable

### Changed

- upgrade for IDEA 2023.1

## 1.3.2 - 2022-12-29

### Added

- copy selected key value to clipboard
- copy all selected values to clipboard

### Changed

- table column width now optimized by default
- last open query tab prevent closing

### Fixed

- connection testing now displays the result of test
- action "Search Parent" in context menu now enabled

## 1.3.1 - 2022-10-31

### Fixed

- make plugin usable for IDEA 2022.3+

## 1.3.0 - 2022-10-31

### Added

- add shortcuts for increment the colum size
- add result table pagination

### Changed

- stabilize plugin
- every query, query-settings and query-options run in his own context

### Fixed

- use scope area name instead of scope displayname

## 1.2.0 - 2022-10-08

### Added

- queries and result displayed in tabs (tabbed pane)

### Changed

- stabilize plugin

### Fixed

- some minor bugs fixed

## 1.1.0 - 2022-10-04

### Added

- enable version query (incl. aggregation)

### Changed

- exception throws by masterdata query now displays as error bubble

## 1.0.1 - 2022-10-03

### Changed

- ui redesign

## 1.0.0 - 2022-09-20

### Added

- first release
