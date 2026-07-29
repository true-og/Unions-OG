# Unions-OG Changelog

This changelog records only changes made by the Unions-OG soft fork relative
to upstream [SimpleClans](https://github.com/RoinujNosde/SimpleClans). Changes
inherited unchanged from SimpleClans are intentionally omitted.

## [2.3] - Initial release

### Added

- Added `/union` and `/unions` as primary command aliases while retaining
  `/clan` and `/clans`.
- Added `/u [message]` for union chat. Running `/u` without a message toggles
  the player's union-chat channel.
- Added `/union color <color>` with named, legacy, and hex color support,
  governed by `unionsog.leader.color`.
- Added `simpleunions` PlaceholderAPI aliases alongside the legacy
  `simpleclans` identifier.
- Added MiniPlaceholders support through Utilities-OG, including clan/union
  aliases and relational placeholders.
- Added a stable `&8None` result for the no-union color-tag placeholder.
- Added a Union Banking preview to the main GUI.
- Added a configuration-gated legacy database migration from `unions_parties` and
  `unions_players` into the configured Unions-OG tables. The migration:
  - Never modifies the legacy source tables.
  - Requires empty, transactional destination tables.
  - Runs in a transaction and records successful application for idempotency.
  - Preserves membership and leader relationships, resolves duplicate tags,
    and supplies deterministic player-name placeholders when needed.
  - Aborts plugin startup on configuration, connection, validation, or import
    failure so the server cannot continue with partially migrated data.
  - Includes unit and database-backed tests for mapping, idempotency, safety,
    and rollback behavior.
- Renamed the plugin and artifact to Unions-OG and changed user-facing clan
  terminology to union.
- Moved the Java and Gradle namespace to `net.trueog.unionsog`, the plugin main
  class to `net.trueog.unionsog.UnionsOG`, and permission nodes to
  `unionsog.*`.
- Disabled verification as a gameplay gate; newly created unions are treated
  as verified while the stored field remains for data compatibility.
- Reserved `None` as a union name or tag to avoid ambiguity with the
  no-union display value.
- Disabled bulletin-board messages on login by default.
- Replaced Vault economy access with DiamondBank-OG and its shard-based player
  balances. Legacy Vault-denominated union balances are zeroed once.
- Temporarily disabled union-bank commands and detail-screen controls until
  DiamondBank-OG provides union-owned accounts; the main GUI shows a preview
  in their place.
- Reordered the main GUI around the union overview, banking preview, lists,
  leaderboards, and administrative tools.
- Replaced the legacy WorldGuard 6 reflection provider with the WorldEdit and
  WorldGuard 7 APIs.
- Updated MySQL driver loading for Connector/J 8, the legacy Connector/J
  class name, MariaDB, and JDBC service discovery.
- Migrated the build from Maven to Gradle with a Java 17 toolchain,
  reproducible shaded jars, formatting checks, and the Utilities-OG and
  DiamondBank-OG submodules.
- Removed the Vault dependency and the scheduled Vault-backed member-fee and
  upkeep tasks.
- Removed the legacy WorldGuard 6 provider.
- Removed bStats telemetry from the fork.

## Fork baseline

The fork last merged upstream SimpleClans history through commit `0406c61b`
on 2026-03-30. Earlier SimpleClans release history belongs to the upstream
project.
