---
description: null
---

# Unions-OG API Example

You can hook into Unions-OG like so:

```java
private UnionsOG unions;

public void onEnable()
{
    Plugin plug = getServer().getPluginManager().getPlugin("Unions-OG");

    if (plug != null)
    {
        unions = ((UnionsOG) plug);
    }
}
```

```java
public void doUnionStuff(Player player)
{
    // get a player's union

    if (unions != null)
    {
        UnionPlayer up = unions.getUnionManager().getUnionPlayer(player.getUniqueId());

        if (up != null)
        {
            Union union = up.getUnion();
        }
        else
        {
            // player is not in a union
        }
    }

    // get a union from a union tag

    if (unions != null)
    {
        Union union = unions.getUnionManager().getUnion("staff");

        if (union != null)
        {
            // union exists
        }
    }
}
```

Every player has a **UnionPlayer** object which holds all their information,
including their union, and can be used to perform various operations on the
player.

The **Union** object holds all the information for a union and can be used to
perform various operations on the union.

The **UnionManager** holds all the **Unions** and **UnionPlayers** and contains
methods that allow you to retrieve them.

The **ProposalManager** holds the open **Proposal** for each union. Use it to
read what a union is currently voting on, or to open a proposal of your own.

## Renamed from upstream

If you are porting code written against SimpleClans, the types were renamed:

| SimpleClans | Unions-OG |
| :--- | :--- |
| `SimpleClans` | `UnionsOG` |
| `Clan` | `Union` |
| `ClanPlayer` | `UnionPlayer` |
| `ClanManager` | `UnionManager` |
| `getClanManager()` | `getUnionManager()` |
| `getClanPlayer(...)` | `getUnionPlayer(...)` |
| `getClan(...)` | `getUnion(...)` |
| `CreateClanEvent`, `DisbandClanEvent` | `CreateUnionEvent`, `DisbandUnionEvent` |

The `Rank`, `RankPermission` and `PermissionLevel` types, the promotion and
demotion events, and every leader-related method \(`isLeader`, `getLeaders`,
`promote`, `demote`\) were removed along with those features.

Database identifiers are unchanged: the table prefix is still `sc_` and the
union table is still `clans`.
