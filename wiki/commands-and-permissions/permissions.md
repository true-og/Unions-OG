---
description: null
---

# Permissions

All Unions-OG permissions live under `unionsog.*`.

> [!IMPORTANT]
> Unions are flat. There is no `unionsog.leader.*` tree, and no rank,
> promotion or demotion nodes. Everything a member can do inside their union
> lives under `unionsog.member.*`.

## Node groups \(some auto added\)

These group nodes are the quick way to set Unions-OG up. If you want to grant
permissions individually, deny the group node and add the individual nodes
instead.

| Permission | Default | Description |
| :--- | :--- | :--- |
| `unionsog.anyone.*` | everyone | Permissions for anyone |
| `unionsog.member.*` | everyone | Permissions for those who can be union members |
| `unionsog.member.land.*` | off | Permissions for allowing/blocking actions in claimed land |
| `unionsog.mod.*` | operators | Permissions for moderators |
| `unionsog.admin.*` | operators | Permissions for admins |

## Individual nodes

You do not need to add these if you already granted the group nodes above.
They are listed so you can toggle individual permissions off.

### Anyone nodes

| Permission | Description |
| :--- | :--- |
| `unionsog.anyone.alliances` | Can view alliances by union |
| `unionsog.anyone.invite-toggle` | Can toggle union invites on/off |
| `unionsog.anyone.leaderboard` | Can view the leaderboard |
| `unionsog.anyone.list` | Can list unions |
| `unionsog.anyone.list.balance` | Can list unions by balance |
| `unionsog.anyone.locale` | Can set their language |
| `unionsog.anyone.lookup` | Can look up a player's info |
| `unionsog.anyone.profile` | Can view a union's profile |
| `unionsog.anyone.rivalries` | Can view rivalries by union |
| `unionsog.anyone.roster` | Can view a union's member list |

### Member nodes

| Permission | Description |
| :--- | :--- |
| `unionsog.member.can-join` | Can join unions |
| `unionsog.member.create` | Can create unions |
| `unionsog.member.resign` | Can resign from their union |
| `unionsog.member.rename` | Can rename their union |
| `unionsog.member.description` | Can modify their union's description |
| `unionsog.member.color` | Can set their union's color |
| `unionsog.member.modtag` | Can modify their union's tag |
| `unionsog.member.coloredtag` | Can use color codes in tags |
| `unionsog.member.setbanner` | Can set their union's banner |
| `unionsog.member.invite` | Can invite players into their union |
| `unionsog.member.kick` | Can kick members from their union |
| `unionsog.member.ally-set` | Can add and remove their union's allies |
| `unionsog.member.rival` | Can start and end a rivalry with another union |
| `unionsog.member.union-ff` | Can toggle their union's friendly fire |
| `unionsog.member.ff` | Can toggle their own friendly fire |
| `unionsog.member.home` | Can teleport to the home base |
| `unionsog.member.home-set` | Can propose moving the home base, and clear it |
| `unionsog.member.regroup.home` | Can regroup the union to the home base |
| `unionsog.member.regroup.me` | Can regroup the union to themself |
| `unionsog.member.discord.create` | Can create a Discord channel for their union |
| `unionsog.member.land` | Can use land commands |
| `unionsog.member.bank` | Can use union banks |
| `unionsog.member.bb` | Can view their union's bulletin board |
| `unionsog.member.bb-add` | Can post to the bulletin board |
| `unionsog.member.bb-clear` | Can clear the bulletin board |
| `unionsog.member.bb-toggle` | Can toggle the bulletin board on login |
| `unionsog.member.chat` | Can use union chat |
| `unionsog.member.chat.color` | Can use colors in chat |
| `unionsog.member.chat.format` | Can use formats in chat \(off by default\) |
| `unionsog.member.ally` | Can use ally chat |
| `unionsog.member.coords` | Can view their union's coords |
| `unionsog.member.kills` | Can view their and others' kills |
| `unionsog.member.lookup` | Can view their own player info |
| `unionsog.member.profile` | Can view their own union's profile |
| `unionsog.member.roster` | Can view their own union's member list |
| `unionsog.member.stats` | Can view their union's stats |
| `unionsog.member.vitals` | Can view their union's vitals |
| `unionsog.member.tag-toggle` | Can hide their own union tag |

#### Voting nodes

See [Proposals and Voting](proposals-and-voting.md).

| Permission | Description |
| :--- | :--- |
| `unionsog.member.vote` | Can vote on their union's proposals |
| `unionsog.member.disband` | Can propose disbanding their union |
| `unionsog.member.war` | Can propose a war and answer war requests |

### Mod nodes

| Permission | Description |
| :--- | :--- |
| `unionsog.mod.ban` | Can ban and unban players from the entire plugin |
| `unionsog.mod.bypass` | Can bypass restrictions |
| `unionsog.mod.disband` | Can disband any union, bypassing the vote |
| `unionsog.mod.kick` | Can kick any player |
| `unionsog.mod.rename` | Can rename any union |
| `unionsog.mod.modtag` | Can change any union's tag |
| `unionsog.mod.globalff` | Can turn off global friendly fire protection |
| `unionsog.mod.home` | Can set any union's home |
| `unionsog.mod.hometp` | Can teleport to any union's home |
| `unionsog.mod.keep-items` | Can keep items when teleporting home |
| `unionsog.mod.mostkilled` | Can view most killed counts |
| `unionsog.mod.nopvpinwar` | Can bypass PvP in wars |
| `unionsog.mod.place` | Can manually place players in unions |
| `unionsog.mod.locale` | Can set players' language |
| `unionsog.mod.staffgui` | Can open the staff GUI |
| `unionsog.mod.bb` | Can view any union's bulletin board |
| `unionsog.mod.bb-add` | Can post to any union's bulletin board |
| `unionsog.mod.bb-clear` | Can clear any union's bulletin board |

### Admin nodes

| Permission | Description |
| :--- | :--- |
| `unionsog.admin.reload` | Can reload the configuration |
| `unionsog.admin.purge` | Can purge a player |
| `unionsog.admin.permanent` | Can toggle a union's permanent status |
| `unionsog.admin.resetkdr` | Can reset a player's or everyone's KDR |
| `unionsog.admin.all-seeing-eye` | Can see all union chats |
| `unionsog.admin.bank.status` | Can check a union's balance |
| `unionsog.admin.bank.give` | Can give money to a union |
| `unionsog.admin.bank.take` | Can take money from a union |
| `unionsog.admin.bank.set` | Can set a union's balance |

### Other nodes

| Permission | Description |
| :--- | :--- |
| `unionsog.other.kdr-exempt` | The player's KDR is not affected on killing/dying \(see Known Issues\). Off by default |
| `unionsog.vip.resetkdr` | Can reset their own KDR |
| `unionsog.vip.teleport-delay` | Bypasses the teleport delay |

## Removed nodes

The upstream nodes for leaders, ranks, trust, verification and member fees no
longer exist. If your permission plugin still grants `simpleclans.*` or
`unionsog.leader.*` nodes, they are inert and can be deleted.
