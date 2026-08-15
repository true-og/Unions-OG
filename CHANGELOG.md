# Unions-OG Changelog

This changelog records only changes made by the Unions-OG soft fork relative
to upstream [SimpleClans](https://github.com/RoinujNosde/SimpleClans). Changes
inherited unchanged from SimpleClans are intentionally omitted.

## [2.5.1] - 2026-08-15

- Inactivity purges removed. Unions and player data are no longer deleted on startup.

## [2.5] - 2026-08-13

### Added

- Clickable chat buttons for request responses. Accept and deny regroup requests
  now display green `[ACCEPT]` and red `[DENY]` buttons in chat that players can
  click instead of typing commands. Hover text explains each button's action.
  Command-based responses still work for full backward compatibility.
- Adventure/MiniMessage dependency for rich text rendering and interactive chat
  components.
- Fix union commands and help menu

## [2.4] - 2026-08-10

### Added

- Unions decide by member vote. `disband`, `home set` and `war start` now open
  a proposal that passes when half the members are in favour, is rejected once
  half becomes unreachable, and expires after 7 days. Votes are stored in the
  database, so members can vote when they next log in.
- `/union vote` shows the open proposal and its tally; `/union vote yes|no`
  casts a vote. A vote screen in the GUI does the same, and is shown once on
  login to members who have not voted.
- Disbanding a union splits its bank evenly between all members. Inert until
  union bank accounts are enabled.
- `/clan`, `/clans`, `/factions`, `/guilds`, `/party`, `/tribe`, `/nation`,
  `/crew`, `/squad` and `/gang` tell the player to use `/union` instead.
  `/f`, `/faction` and `/guild` are left to Utilities-OG.

### Changed

- Unions are flat. Every union command is member-level, and the three actions
  above are the only ones needing more than one member's say-so.
- Folded `unionsog.leader.*` into `unionsog.member.*`, which is granted by
  default. Union ally management is `unionsog.member.ally-set` and union
  friendly fire is `unionsog.member.union-ff`, to avoid clashing with the
  existing ally-chat and personal-friendly-fire nodes.
- Renamed clan to union across the API, config sections and message keys:
  `Clan` is `Union`, `clan:` is `union:`, `clanchat:` is `unionchat:`.
  Database identifiers and PlaceholderAPI placeholder names deliberately keep
  their `clan` spelling.
- Renamed `/union clanff` to `/union unionff`.
- Chat and display formats accept `{union}`; `{clan}` still works.
- A union now ends with its last member.
- `/union war start` no longer asks the target union, so the member-count
  guard always applies. Removed `war-and-protection.war-start.request-enabled`.
- Inter-union requests are answered by the first member of the asked union
  rather than by its leaders.
- Set the union GUI title to "Union Management".

### Removed

- Ranks, promotion and demotion, with their commands, settings, events and GUI
  screens.
- The leader role: no leader flag, leader-only command, tag colour, Discord
  role or `sc_leader` LuckPerms group. The `sc_players.leader` column is kept
  and always written `0` for data compatibility.
- The leaders-only land creation option.
- The in-game Crowdin translation prompts.

### Fixed

- `/union lookup` and `/union profile` no longer print a literal
  `%player_rank%` or `%clan_leaders%`.
- Removed a stale `%rank%` token from the default chat formats.
- `/union unionff block` now requires union membership, matching
  `/union unionff allow`.
- The legacy database importer no longer reads or writes leader relationships.

## [2.3] - Initial release

### Added

- `/union` and `/unions` as primary command aliases, keeping `/clan` and
  `/clans`.
- `/u [message]` for union chat. Running `/u` alone toggles the channel.
- `/union color <color>` with named, legacy and hex color support.
- `simpleunions` PlaceholderAPI aliases alongside the legacy `simpleclans`
  identifier, plus MiniPlaceholders support through Utilities-OG.
- A Union Banking preview in the main GUI.
- A configuration-gated legacy database migration from `unions_parties` and
  `unions_players`. It never touches the source tables, requires empty
  transactional destinations, is idempotent, and aborts startup on failure so
  the server cannot run on partially migrated data.

### Changed

- Renamed the plugin and artifact to Unions-OG, moved the namespace to
  `net.trueog.unionsog` and permission nodes to `unionsog.*`.
- Replaced Vault with DiamondBank-OG and its shard-based balances. Legacy
  Vault-denominated union balances are zeroed once. Union-bank commands are
  disabled until DiamondBank-OG provides union-owned accounts.
- Unions are treated as verified without the upstream verification workflow.
- Reserved `None` as a union name or tag, and made the no-union color-tag
  placeholder return a stable `&8None`.
- Bulletin-board messages no longer show on login by default.
- Reordered the main GUI around the union overview, banking preview, lists,
  leaderboards and administrative tools.
- Replaced the WorldGuard 6 reflection provider with the WorldEdit and
  WorldGuard 7 APIs.
- Updated MySQL driver loading for Connector/J 8, MariaDB and JDBC service
  discovery.
- Migrated the build from Maven to Gradle with a Java 17 toolchain,
  reproducible shaded jars, and the Utilities-OG and DiamondBank-OG submodules.

### Removed

- The Vault dependency, along with the member-fee and upkeep tasks it backed.
- The legacy WorldGuard 6 provider.
- bStats telemetry.

## Fork baseline

The fork last merged upstream SimpleClans history through commit `0406c61b`
on 2026-03-30. Earlier SimpleClans release history belongs to the upstream
project.
