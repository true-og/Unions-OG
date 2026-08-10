![Unions-OG Logo](https://i.imgur.com/9vgfVdX.png)

# Unions-OG

A full-featured union system for PvP Minecraft servers.

> [!IMPORTANT]
> Unions-OG is an unofficial soft fork of
> [SimpleClans](https://github.com/RoinujNosde/SimpleClans), maintained for the
> TrueOG Network. Upstream behavior is retained where practical, while
> branding, integrations, and selected gameplay behavior intentionally differ.

Current fork version: **2.4**

- [Fork-only changelog](CHANGELOG.md)
- [PlaceholderAPI and MiniPlaceholders reference](placeholder-info.txt)
- [Upstream SimpleClans project](https://github.com/RoinujNosde/SimpleClans)

## Main fork differences

### Flat unions

Unions have no internal hierarchy. Ranks, promotion, demotion and the leader
role itself were removed, so every member holds the same powers.

- Every union command is member-level. There is no leader flag, no rank
  system, and no way to grant one member authority over another.
- A union ends with its last member: resigning, or being kicked or moved out
  as the only member, disbands it.

### Decisions by member consensus

Three actions need a vote of the union's own members instead of a leader's
say-so: **disband**, **home set**, and **war start**.

- A proposal passes as soon as half the members (rounded up) vote in favour,
  and is rejected as soon as enough vote against that half can no longer be
  reached.
- Votes are stored in the database, so a member who is offline when a
  proposal opens can still vote the next time they log in. Proposals expire
  after 7 days.
- A union can only have one proposal open at a time.
- Members vote with `/union vote yes|no`, or by clicking the vote buttons in
  the union GUI. The GUI is presented once on login to members who have not
  voted yet.
- `/union vote` on its own shows the open proposal and the running tally.

### Other differences

- Uses Unions-OG branding and union terminology throughout, including config
  sections (`union:`, `unionchat:`) and message keys.
- Provides `/union` and `/unions`. Other group words — `/clan`, `/clans`,
  `/factions`, `/guilds`, `/party`, `/tribe`, `/nation`, `/crew`, `/squad` and
  `/gang` — are registered as signposts that tell the player to run the same command with
  `/union` instead. `/f`, `/faction` and `/guild` belong to Utilities-OG,
  which already redirects them.
- Uses `/u` for union chat; a bare `/u` toggles the union-chat channel.
- Adds `/union rename <name>` and `/union color <color>`, both open to any
  member.
- Union-wide friendly fire is `/union unionff allow|block`.
- Supports both `simpleclans` and `simpleunions` placeholder identifiers in
  PlaceholderAPI and MiniPlaceholders.
- Uses DiamondBank-OG instead of Vault for player economy access. Union-owned
  bank accounts remain under development, so their commands are currently
  disabled. Once enabled, disbanding a union splits its bank evenly between
  all members.
- Treats unions as verified without requiring the upstream verification
  workflow.
- Includes a configuration-gated, transactional importer for the legacy
  unions database.

See the [changelog](CHANGELOG.md) for the complete list of changes maintained
by this fork. Upstream SimpleClans changes are intentionally not repeated
there.

## Documentation

The [Unions-OG wiki](wiki/) documents this fork, and is the reference to use.

- [Commands](wiki/commands-and-permissions/commands.md)
- [Permissions](wiki/commands-and-permissions/permissions.md)
- [Proposals and Voting](wiki/commands-and-permissions/proposals-and-voting.md)
- [Union Alliances and Rivalries](wiki/commands-and-permissions/alliances-and-rivalries.md)
- [Configuration](wiki/how-to-setup/configuration.md)
- [PlaceholderAPI Support](wiki/other/placeholderapi-support.md)
- [API example](wiki/other/unions-og-api.md)

> [!WARNING]
> The [upstream SimpleClans documentation](https://simpleclans.gitbook.io/simpleclans/)
> no longer describes this fork. Its pages on leaders, ranks, promotion,
> demotion, member fees and upkeep do **not** apply, and its command,
> permission and configuration names have all been renamed here.

For the unmodified upstream plugin and its public releases, use the
[SimpleClans Spigot page](https://www.spigotmc.org/resources/simpleclans.71242/).
That download is not an Unions-OG build.

## Building

The build requires Java 17. Clone with submodules, then run:

```bash
git submodule update --init --recursive
./gradlew build
```

The shaded plugin is produced under `build/libs/` with the
`Unions-OG-2.4.jar` name.

## Developer compatibility

- Java and Gradle namespace: `net.trueog.unionsog`
- Bukkit main class: `net.trueog.unionsog.UnionsOG`
- API types use union naming: `Union`, `UnionPlayer`, `UnionManager`,
  `Proposal`, `DisbandUnionEvent`
- Permission prefix: `unionsog.*`. Union actions all live under
  `unionsog.member.*`, which is granted by default; there is no
  `unionsog.leader.*` tree
- Command aliases: `union` and `unions`. Other group words are redirect-only
  signposts, not aliases, and never execute a union command
- Placeholder identifiers: `simpleunions` and the retained `simpleclans`
  compatibility identifier. Placeholder **names** deliberately keep their
  upstream `clan` spelling (`%simpleunions_clan_name%`) so existing setups
  keep resolving
- Database identifiers are unchanged for live-data compatibility: the table
  prefix stays `sc_`, and the `clans` table and `leader` column are retained
  even though the leader column is no longer used
- Chat and display formats accept `{union}`; the legacy `{clan}` token still
  works

The old SimpleClans Maven coordinates and Java packages do not describe this
fork and should not be used as Unions-OG API coordinates.

## Credits and license

Unions-OG is derived from SimpleClans by its original and upstream
contributors. This fork remains licensed under the [GNU GPL v3](LICENSE).
