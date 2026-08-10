---
description: null
---

# Configuration

This page lists every option in `config.yml` as the plugin ships it. Each
section shows the option names followed by the shipped defaults.

> [!NOTE]
> Options for ranks, promotion, demotion, leaders, member fees and upkeep no
> longer exist. If you are upgrading from SimpleClans or an older Unions-OG,
> those keys are ignored and can be deleted.

## Safe Civilians

* `safe-civilians`

### Example

```yaml
safe-civilians: false
```

## Tags Format

* `default-color`
* `max-length`
* `min-length`
* `bracket`
  * `color`
  * `left`
  * `right`
* `separator`
  * `color`
  * `char`

### Example

```yaml
tag:
    default-color: '8'
    max-length: 5
    min-length: 2
    bracket:
        color: '8'
        left: ''
        right: ''
    separator:
        color: '8'
        char: ' .'
```

## General Settings

* `enable-gui`
* `disable-messages`
* `tameable-mobs-sharing`
* `teleport-blocks`
* `teleport-home-on-spawn`
* `drop-items-on-union-home`
* `keep-items-on-union-home`
* `item-list`
* `show-debug-info`
* `enable-auto-groups`
* `chat-compatibility-mode`
* `rival-limit-percent`
* `use-colorcode-from-prefix-for-name`
* `accept-other-alphabets-letters-on-tag`
* `display-chat-tags`
* `global-friendly-fire`
* `unrivable-unions`
* `blacklisted-worlds`
* `banned-players`
* `disallowed-tags`
* `language`
* `user-language-selector`
* `disallowed-tag-colors`
* `server-name`
* `allow-regroup-command`
* `allow-reset-kdr`
* `rejoin-cooldown`
* `rejoin-cooldown-enabled`
* `ranking-type`
* `list-default-order-by`
* `lore-length`
* `past-unions-limit`
* `username-regex`
* `tag-regex`
* `date-time-pattern`
* `bungee-servers`

### Example

```yaml
settings:
    enable-gui: true
    disable-messages: false
    tameable-mobs-sharing: false
    teleport-blocks: false
    teleport-home-on-spawn: false
    drop-items-on-union-home: false
    keep-items-on-union-home: false
    item-list: []
    show-debug-info: false
    enable-auto-groups: false
    chat-compatibility-mode: true
    rival-limit-percent: 50
    use-colorcode-from-prefix-for-name: true
    accept-other-alphabets-letters-on-tag: false
    display-chat-tags: true
    global-friendly-fire: false
    unrivable-unions:
    - admin
    - staff
    - mod
    blacklisted-worlds: []
    banned-players: []
    disallowed-tags:
    - vip
    - union
    language: en
    user-language-selector: true
    disallowed-tag-colors:
    - '4'
    - 'k'
    server-name: '&4Unions-OG'
    allow-regroup-command: true
    allow-reset-kdr: false
    rejoin-cooldown: 60
    rejoin-cooldown-enabled: false
    ranking-type: "DENSE"
    list-default-order-by: size
    lore-length: 36
    past-unions-limit: 10
    username-regex: '(\.|\*){0,1}[a-zA-Z0-9_$]{1,16}'
    tag-regex: ''
    date-time-pattern: 'HH:mm - dd/MM/yyyy'
    bungee-servers: []
```

## War and Protection

* `war-enabled`
* `land-sharing`
* `protection-providers`
* `listeners`
  * `priority`
  * `ignored-list`
    * `PLACE`
    * `BREAK`
* `set-base-only-in-land`
* `war-normal-expiration-time`
* `war-disconnect-expiration-time`
* `edit-all-lands`
* `land-creation`
  * `only-one-per-union`
* `war-actions`
  * `CONTAINER`
  * `INTERACT`
  * `BREAK`
  * `PLACE`
  * `DAMAGE`
  * `INTERACT_ENTITY`
* `war-start`
  * `members-online-max-difference`

### Example

```yaml
war-and-protection:
    war-enabled: false
    land-sharing: true
    protection-providers:
        - WorldGuardProvider
        - GriefPreventionProvider
    listeners:
        priority: HIGHEST
        ignored-list:
            PLACE:
                - "PLAYER_HEAD"
            BREAK:
                - "EMERALD_BLOCK"
    set-base-only-in-land: false
    war-normal-expiration-time: 0
    war-disconnect-expiration-time: 0
    edit-all-lands: false
    land-creation:
        only-one-per-union: false
    war-actions:
        CONTAINER: true
        INTERACT: true
        BREAK: true
        PLACE: true
        DAMAGE: true
        INTERACT_ENTITY: true
    war-start:
        members-online-max-difference: 5
```

## KDR Grinding Prevention

* `enable-max-kills`
* `max-kills-per-victim`
* `enable-kill-delay`
* `delay-between-kills`

### Example

```yaml
kdr-grinding-prevention:
    enable-max-kills: false
    max-kills-per-victim: 10
    enable-kill-delay: false
    delay-between-kills: 5
```

## General Commands

* `more`
* `ally`
* `union`
* `accept`
* `deny`
* `global`
* `union_chat`
* `force-priority`

### Example

```yaml
commands:
    more: more
    ally: ally
    union: union
    accept: accept
    deny: deny
    global: global
    union_chat: u
    force-priority: true
```

## Economy

* `creation-price`
* `purchase-union-create`
* `invite-price`
* `purchase-union-invite`
* `home-teleport-price`
* `purchase-home-teleport`
* `home-teleport-set-price`
* `purchase-home-teleport-set`
* `home-regroup-price`
* `purchase-home-regroup`
* `unique-tax-on-regroup`
* `issuer-pays-regroup`
* `bank-log`
  * `enable`

### Example

```yaml
economy:
    creation-price: 100.0
    purchase-union-create: false
    invite-price: 20.0
    purchase-union-invite: false
    home-teleport-price: 5.0
    purchase-home-teleport: false
    home-teleport-set-price: 5.0
    purchase-home-teleport-set: false
    home-regroup-price: 5.0
    purchase-home-regroup: false
    unique-tax-on-regroup: true
    issuer-pays-regroup: true
    bank-log:
        enable: true
```

## Kill Weights

* `rival`
* `civilian`
* `neutral`
* `ally`
* `deny-same-ip-kills`

### Example

```yaml
kill-weights:
    rival: 2.0
    civilian: 0.0
    neutral: 1.0
    ally: -1.0
    deny-same-ip-kills: false
```

## Union Settings

* `homebase-teleport-wait-secs`
* `homebase-can-be-set-only-once`
* `min-size-to-set-rival`
* `max-length`
* `max-description-length`
* `min-description-length`
* `max-members`
* `trust-members-by-default`
* `ff-on-by-default`
* `min-length`
* `max-alliances`
* `min-size-to-set-ally`

### Example

```yaml
union:
    homebase-teleport-wait-secs: 5
    homebase-can-be-set-only-once: true
    min-size-to-set-rival: 3
    max-length: 25
    max-description-length: 120
    min-description-length: 10
    max-members: 25
    trust-members-by-default: true
    ff-on-by-default: false
    min-length: 2
    max-alliances: -1
    min-size-to-set-ally: 3

```

## Page

* `untrusted-color`
* `union-name-color`
* `subtitle-color`
* `headings-color`
* `trusted-color`
* `separator`
* `size`
* `help-size`

### Example

```yaml
page:
    untrusted-color: '8'
    union-name-color: b
    subtitle-color: '7'
    headings-color: '8'
    trusted-color: f
    separator: '-'
    size: 100
    help-size: 10
```

## Union Chat

* `enable`
* `tag-based-union-chat`
* `announcement-color`
* `format`
* `spy-format`
* `trusted-color`
* `member-color`
* `listener-priority`

### Example

```yaml
unionchat:
    enable: true
    tag-based-union-chat: false
    announcement-color: e
    format: "&b[%union%&b] &4<%nick-color%%player%&4>: &b%message%"
    spy-format: "&8[Spy] [&bC&8] <%clean-tag%&8> <%nick-color%*&8%player%>&8: %message%"
    trusted-color: 'f'
    member-color: '7'
    listener-priority: LOW
```

## Request

* `message-color`
* `ask-frequency-secs`
* `max-asks-per-request`

### Example

```yaml
request:
    message-color: b
    ask-frequency-secs: 60
    max-asks-per-request: 1440
```

## Bulletin Board

* `prefix`
* `show-on-login`
* `size`
* `login-size`

### Example

```yaml
bb:
    prefix: "&8* &e"
    show-on-login: false
    size: 6
    login-size: 6
```

## Ally Chat

* `enable`
* `format`
* `spy-format`
* `trusted-color`
* `member-color`

### Example

```yaml
allychat:
    enable: true
    format: "&b[Ally Chat] &4<%union%&4> <%nick-color%%player%&4>: &b%message%"
    spy-format: "&8[Spy] [&cA&8] <%clean-tag%&8> <%nick-color%*&8%player%>&8: %message%"
    trusted-color: 'f'
    member-color: '7'
```

## Discord Chat

* `enable`
* `auto-creation`
* `discord-format`
* `format`
* `spy-format`
* `min-linked-players-to-create`
* `text`
  * `category-format`
  * `category-ids`
  * `whitelist`
  * `unions-limit`

### Example

```yaml
discordchat:
    enable: false
    auto-creation: true
    discord-format: "%player% » %message%"
    format: "&b[&9D&b] &b[%union%&b] &4<%nick-color%%player%&4>: &b%message%"
    spy-format: "&8[Spy] [&9D&8] <%clean-tag%&8> <%nick-color%*&8%player%>&8: %message%"
    min-linked-players-to-create: 3
    text:
        category-format: "SC - TextChannels"
        category-ids: []
        whitelist: []
        unions-limit: 100
```

## Purge Data

* `inactive-player-data-days`
* `inactive-union-days`

### Example

```yaml
purge:
    inactive-player-data-days: 30
    inactive-union-days: 7
```

## mySQL Settings

* `username`
* `host`
* `port`
* `enable`
* `password`
* `database`
* `table_prefix`
* `migrate-legacy-unions-database`

### Example

```yaml
mysql:
    username: '^{MARIADB_USER}'
    host: localhost
    port: 3306
    enable: true
    password: '^{MARIADB_PASSWORD}'
    database: 'unions'
    table_prefix: 'sc_'
    # One-time, source-read-only import from the legacy unions schema.
    # The <table_prefix>clans, <table_prefix>players, and <table_prefix>kills
    # destination tables must be empty and use InnoDB.
    migrate-legacy-unions-database: false
```

## Permissions

* `auto-group-groupname`
* `YourClanNameHere`

### Example

```yaml
permissions:
  auto-group-groupname: false
  YourClanNameHere:
  - test.permission
```

## Performance

* `save-periodically`
* `save-interval`
* `use-threads`
* `use-bungeecord`

### Example

```yaml
performance:
  save-periodically: true
  save-interval: 10
  use-threads: true
  use-bungeecord: false
```
