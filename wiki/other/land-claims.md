---
description: null
---

# Land Claims Plugins

## Configuration

* `enable-auto-groups` - whether Unions-OG adds each member to a
  `union_<uniontag>` permission group
* `auto-group-groupname` - whether the union tag is also added as a bare
  `group.<uniontag>` permission

### Exemple

```yaml
settings:
    enable-auto-groups: false
permissions:
  auto-group-groupname: true
  YourUnionTagHere:
  - test.permission
```

## GriefPrevention

You can replace `<uniontag>` with ANY union tag \(ally, rival, etc\)

| Command | Description |
| :--- | :--- |
| `/Trust group.<uniontag>` | Gives the Union members permission to edit in your claim |
| `/AccessTrust group.<uniontag>` | Gives the Union members permission to use your buttons, levers, and beds |
| `/ContainerTrust group.<uniontag>` | Gives the Union members permission to use your buttons, levers, beds, crafting gear, containers, and animals |
| `/PermissionTrust group.<uniontag>` | Gives the Union members permission to share their permission level with others |
| `/UnTrust group.<uniontag>` | Revokes any permissions granted to a union in your claim |

## Note

After the permission is given, the player must reconnect.

