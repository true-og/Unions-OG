---
description: null
---

# Commands

> Required arguments are marked with `(argument)`
>
> Optional arguments are marked with `[argument]`

`/union` is the primary command, with `/unions` as its only alias.

Words other servers use for the same thing — `/clan`, `/clans`, `/factions`,
`/guilds`, `/party`, `/tribe`, `/nation`, `/crew`, `/squad` and `/gang` — are
registered as signposts: running one tells the player to use `/union`
instead, carrying their arguments over, and does nothing else. `/f`,
`/faction` and `/guild` are redirected by Utilities-OG rather than by this
plugin.

## Anyone Commands

| Command | Description |
| :--- | :--- |
| `/union` | Opens the GUI or shows the help |
| `/union help` | Shows the plugin's commands |
| `/union create [tag] [name]` | Creates a new union |
| `/accept` | Accepts a request |
| `/deny` | Denies a request |
| `/more` | Shows more information |
| `/union leaderboard` | Shows the leaderboard |
| `/union list [name\|size\|kdr\|founded\|active] [asc\|desc]` | Lists all unions |
| `/union list balance` | Lists unions by balance |
| `/union rivalries` | Shows all union rivalries |
| `/union alliances` | Shows all union alliances |
| `/union lookup [player]` | Looks up your or another player's info |
| `/union profile [tag]` | Shows a union's profile |
| `/union roster [tag]` | Shows a union's roster |
| `/union locale (language)` | Sets your language |
| `/union ff (allow\|auto)` | Toggles personal friendly fire |
| `/union toggle invite` | Toggles whether you receive union invites |
| `/union resetkdr` | Resets your KDR |

## Member Commands

Unions are flat. Every member may run every union command, apart from the
three that need a vote of the membership.

### General

| Command | Description |
| :--- | :--- |
| `/union kills [player]` | Shows your or another player's kill counts |
| `/union mostkilled` | Shows server-wide most killed counts |
| `/union resign` | Resigns from the union |
| `/union vitals` | Shows your union's vitals |
| `/union stats` | Shows your union's stats |
| `/union coords` | Shows your union's coords |
| `/union bb` | Shows your union's bulletin board |
| `/union bb add (message)` | Posts to the bulletin board |
| `/union bb clear` | Clears the bulletin board |
| `/union toggle bb` | Toggles the bulletin board on login |
| `/union toggle tag` | Hides or shows your union tag |

### Running the union

| Command | Description |
| :--- | :--- |
| `/union rename (name)` | Renames the union |
| `/union description (description)` | Sets the union's description |
| `/union color (color)` | Sets the union's color |
| `/union setbanner` | Sets the union's banner |
| `/union invite (player)` | Invites a player |
| `/union kick (player)` | Kicks a member |
| `/union unionff (allow\|block)` | Toggles union-wide friendly fire |
| `/union home` | Teleports to the union's home |
| `/union home clear` | Clears the union's home |
| `/union regroup me` | Regroups members to your location |
| `/union regroup home` | Regroups members to the union's home |
| `/union ally (add\|remove) (tag)` | Adds or removes an ally |
| `/union rival (add\|remove) (tag)` | Adds or removes a rival |
| `/union war end (tag)` | Proposes ending a war |
| `/union discord create` | Creates a Discord channel for the union |
| `/union land ...` | Manages permissions inside your claimed land |

### Needs a vote

These open a proposal instead of taking effect immediately. See
[Proposals and Voting](proposals-and-voting.md).

| Command | Description |
| :--- | :--- |
| `/union disband` | Proposes disbanding the union |
| `/union home set` | Proposes moving the union's home |
| `/union war start (tag)` | Proposes declaring war on a rival |
| `/union vote` | Shows the open proposal and its tally |
| `/union vote (yes\|no)` | Casts your vote |

### Chat

| Command | Description |
| :--- | :--- |
| `/u (message)` | Sends a message to your union's chat |
| `/u [join\|leave\|mute]` | Joins, leaves or mutes your union's chat |
| `/ally (message)` | Sends a message to the ally chat |
| `/ally [join\|leave\|mute]` | Joins, leaves or mutes the ally chat |

### Bank

> Union bank accounts are under development and these commands are currently
> disabled. When they are enabled, disbanding a union splits its balance
> evenly between all members.

| Command | Description |
| :--- | :--- |
| `/union bank status` | Shows the union's balance |
| `/union bank deposit (amount\|all)` | Deposits into the union bank |
| `/union bank withdraw (amount\|all)` | Withdraws from the union bank |

## Mod Commands

| Command | Description |
| :--- | :--- |
| `/union mod place (player) (union)` | Places a player in a union |
| `/union mod rename (union) (name)` | Renames a union |
| `/union mod kick (player)` | Kicks a player from their union |
| `/union mod disband (union)` | Disbands a union, bypassing the vote |
| `/union mod home set (tag)` | Sets a union's home |
| `/union mod home tp (tag)` | Teleports to a union's home |
| `/union mod ban (player)` | Bans a player from union commands |
| `/union mod unban (player)` | Unbans a player from union commands |
| `/union mod globalff (allow\|auto)` | Toggles the global friendly-fire status |
| `/union mod modtag (union) (tag)` | Changes a union's tag |
| `/union mod locale (player) (language)` | Sets a player's language |
| `/union mod bb display (union)` | Shows a union's bulletin board |
| `/union mod bb add (union) (message)` | Posts to a union's bulletin board |
| `/union mod bb clear (union)` | Clears a union's bulletin board |

## Admin Commands

| Command | Description |
| :--- | :--- |
| `/union admin reload` | Reloads the plugin and its configuration \(some features may need a server restart\) |
| `/union admin purge (player)` | Purges a player's data |
| `/union admin permanent (union)` | Toggles a union's permanent status |
| `/union admin resetkdr (player)` | Resets a player's KDR |
| `/union admin resetkdr everyone` | Resets everyone's KDR |
| `/union admin bank status (union)` | Shows a union's balance |
| `/union admin bank give (union) (amount)` | Gives money to a union |
| `/union admin bank take (union) (amount)` | Takes money from a union |
| `/union admin bank set (union) (amount)` | Sets a union's balance |

## Removed commands

These upstream SimpleClans commands do not exist in Unions-OG.

| Command | Reason |
| :--- | :--- |
| `/clan promote`, `/clan demote` | Unions have no leaders |
| `/clan admin promote`, `/clan admin demote` | Unions have no leaders |
| `/clan rank ...` | Ranks were removed |
| `/clan trust`, `/clan untrust` | Trust levels were removed |
| `/clan verify`, `/clan mod verify` | Unions are always treated as verified |
| `/clan fee ...` | Member fees were removed |
