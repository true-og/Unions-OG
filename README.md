![Unions-OG Logo](https://i.imgur.com/9vgfVdX.png)

# Unions-OG

A full-featured union system for PvP Minecraft servers.

> [!IMPORTANT]
> Unions-OG is an unofficial soft fork of
> [SimpleClans](https://github.com/RoinujNosde/SimpleClans), maintained for the
> TrueOG Network. Upstream behavior is retained where practical, while
> branding, integrations, and selected gameplay behavior intentionally differ.

Current fork version: **2.3**

- [Fork-only changelog](CHANGELOG.md)
- [PlaceholderAPI and MiniPlaceholders reference](placeholder-info.txt)
- [Upstream SimpleClans project](https://github.com/RoinujNosde/SimpleClans)

## Main fork differences

- Uses Unions-OG branding and union terminology.
- Provides `/union` and `/unions` while keeping `/clan` and `/clans` as
  compatibility aliases.
- Uses `/u` for union chat; a bare `/u` toggles the union-chat channel.
- Adds `/union color <color>` with the `unionsog.leader.color` permission.
- Supports both `simpleclans` and `simpleunions` placeholder identifiers in
  PlaceholderAPI and MiniPlaceholders.
- Uses DiamondBank-OG instead of Vault for player economy access. Union-owned
  bank accounts remain under development, so their commands are currently
  disabled.
- Treats unions as verified without requiring the upstream verification
  workflow.
- Includes a configuration-gated, transactional importer for the legacy
  unions database.

See the [changelog](CHANGELOG.md) for the complete list of changes maintained
by this fork. Upstream SimpleClans changes are intentionally not repeated
there.

## Documentation

The [SimpleClans documentation](https://simpleclans.gitbook.io/simpleclans/)
remains the baseline reference for inherited features. It uses upstream clan
terminology, and fork-specific commands, permissions, banking behavior, and
integrations may differ.

- [Upstream commands](https://simpleclans.gitbook.io/simpleclans/commands-and-permissions/commands)
- [Upstream permissions](https://simpleclans.gitbook.io/simpleclans/commands-and-permissions/permissions)
- [Upstream configuration](https://simpleclans.gitbook.io/simpleclans/how-to-setup/configuration)
- [Upstream ranks](https://simpleclans.gitbook.io/simpleclans/commands-and-permissions/ranks-with-permissions)
- [Upstream land protection integrations](https://simpleclans.gitbook.io/simpleclans/other/land-claims)

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
`Unions-OG-2.3.jar` name.

## Developer compatibility

- Java and Gradle namespace: `net.trueog.unionsog`
- Bukkit main class: `net.trueog.unionsog.UnionsOG`
- Permission prefix: `unionsog.*`
- Command aliases: `union`, `unions`, `clan`, and `clans`
- Placeholder identifiers: `simpleunions` and the retained `simpleclans`
  compatibility identifier

The old SimpleClans Maven coordinates and Java packages do not describe this
fork and should not be used as Unions-OG API coordinates.

## Credits and license

Unions-OG is derived from SimpleClans by its original and upstream
contributors. This fork remains licensed under the [GNU GPL v3](LICENSE).
