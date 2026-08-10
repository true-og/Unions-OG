---
description: null
---

# Union on Tablist

## Plugins needed

* [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/)
* [PlayerListPlus](https://www.spigotmc.org/resources/%E2%99%9B-playerlistplus-%E2%99%9B-1-8-1-14-3-tablist-editor.55878/) \(you can use a different tablist plugin, as long as it supports PlaceholderAPI\)

## Step by step

1. Open PlayerListPlus config and edit the formats adding `%simpleunions_clan_color_tag%`:

> Placeholder **names** keep their upstream `clan` spelling, so
> `%simpleclans_clan_color_tag%` works identically.

{% code title="Example:" %}
```yaml
slot-items:
#   This slot items will shows all players
 PLAYERS:
     format: "%simpleunions_clan_color_tag%&c.$displayname"
     type: PLAYER_LIST
     hidevanished: true
     ping: true
     skin: true
```
{% endcode %}

1. Restart \(or reload\) and enjoy!

## Screenshot

![](../.gitbook/assets/clans-tablist.png)

> The screenshot predates the Unions-OG rebrand.

