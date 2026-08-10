---
description: null
---

# PlaceholderAPI Support

`Unions-OG` registers two PlaceholderAPI identifiers:

- `%simpleclans_*%` for legacy compatibility
- `%simpleunions_*%` for the rebranded union-facing namespace

Union aliases are accepted under either identifier. For example, `%simpleclans_in_union%`, `%simpleclans_union_name%`, `%simpleunions_union_name%`, and `%simpleunions_topunions_1_union_name%` all resolve correctly.

## Available placeholders

> Placeholder **names** deliberately keep their upstream `clan` spelling so
> existing setups keep working. `%simpleclans_clan_name%`,
> `%simpleunions_clan_name%` and `%simpleunions_union_name%` are all the same
> value.

### Player placeholders

| Placeholder | Result |
| :--- | :--- |
| `%simpleunions_name%` | STRING |
| `%simpleunions_clean_name%` | STRING |
| `%simpleunions_tag%` | STRING |
| `%simpleunions_tag_label%` | STRING |
| `%simpleunions_in_clan%` | BOOLEAN |
| `%simpleunions_is_member%` | BOOLEAN |
| `%simpleunions_is_trusted%` | BOOLEAN |
| `%simpleunions_neutral_kills%` | INTEGER |
| `%simpleunions_rival_kills%` | INTEGER |
| `%simpleunions_civilian_kills%` | INTEGER |
| `%simpleunions_ally_kills%` | INTEGER |
| `%simpleunions_total_kills%` | INTEGER |
| `%simpleunions_weighted_kills%` | INTEGER |
| `%simpleunions_deaths%` | INTEGER |
| `%simpleunions_kdr%` | FLOAT |
| `%simpleunions_join_date%` | DATE |
| `%simpleunions_inactive_days%` | INTEGER |
| `%simpleunions_lastseen%` | INTEGER |
| `%simpleunions_lastseendays%` | INTEGER |
| `%simpleunions_is_bb_enabled%` | BOOLEAN |
| `%simpleunions_is_tag_enabled%` | BOOLEAN |
| `%simpleunions_is_invite_enabled%` | BOOLEAN |
| `%simpleunions_is_friendlyfire_on%` | BOOLEAN |
| `%simpleunions_is_muted%` | BOOLEAN |
| `%simpleunions_is_mutedally%` | BOOLEAN |
| `%simpleunions_clanchat_player_color%` | STRING |
| `%simpleunions_allychat_player_color%` | STRING |
| `%simpleunions_topplayers_position%` | INTEGER |

### Union placeholders

Prefix these with `clan_` or `union_`, for example
`%simpleunions_union_name%`.

| Placeholder | Result |
| :--- | :--- |
| `name` | STRING |
| `tag` | STRING |
| `color_tag` | STRING |
| `bracket_tag` | STRING |
| `color` | STRING |
| `size` | INTEGER |
| `onlinemembers_count` | INTEGER |
| `allies_count` | INTEGER |
| `rivals_count` | INTEGER |
| `founded` | DATE |
| `inactivedays` | INTEGER |
| `friendly_fire` | BOOLEAN |
| `is_anyonline` | BOOLEAN |
| `is_unrivable` | BOOLEAN |
| `is_permanent` | BOOLEAN |
| `balance` | INTEGER |
| `balance_formatted` | STRING |
| `allow_deposit` | BOOLEAN |
| `allow_withdraw` | BOOLEAN |
| `total_kills` | INTEGER |
| `total_neutral` | INTEGER |
| `total_civilian` | INTEGER |
| `total_rival` | INTEGER |
| `total_ally` | INTEGER |
| `total_deaths` | INTEGER |
| `total_kdr` | FLOAT |
| `average_wk` | INTEGER |
| `topclans_position` | INTEGER |

### Leaderboard placeholders

`%simpleunions_topplayers_#_<player placeholder>%` and
`%simpleunions_topunions_#_union_<union placeholder>%`, where `#` is the
position. The legacy `topclans` spelling also works.

### Relational placeholders

`%rel_simpleunions_color%` colours a player's name by their relationship to
the viewer, using the `color.same_clan`, `color.rival` and `color.ally`
settings in PlaceholderAPI's config.

## Removed placeholders

`is_leader`, `leader_size`, `rank`, `rank_displayname` and `has_rank` no longer
exist, because unions have no leaders or ranks.
