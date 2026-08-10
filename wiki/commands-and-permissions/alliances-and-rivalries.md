---
description: null
---

# Union Alliances and Rivalries

Any member can send a request to start an alliance with another union using
`/union ally add`. The alliance forms when any member of the second union
accepts. Either side can break it at any time with `/union ally remove`; no
acceptance is needed to end an alliance.

Rivalries can be started by any union at any time, with no request needed.
`/union rival add` forms the rivalry immediately, so the other union's consent
is not required. Ending one does need their agreement: `/union rival remove`
sends a request, and the rivalry ends when any member of the other union
accepts.

Declaring **war** on a rival is different from either. It affects the whole
union, so it needs a vote of your own members rather than the target's
consent. See [Proposals and Voting](proposals-and-voting.md).

You can view every union and its allies with `/union alliances`, or its rivals
with `/union rivalries`.

## Commands

| Command | Description |
| :--- | :--- |
| `/union ally add [tag]` | Sends a request to start an alliance \(acceptance required\) |
| `/union ally remove [tag]` | Removes an alliance \(no acceptance required\) |
| `/union rival add [tag]` | Starts a rivalry \(no acceptance required\) |
| `/union rival remove [tag]` | Ends a rivalry \(acceptance required\) |
| `/union war start [tag]` | Proposes declaring war on a rival \(union vote required\) |
| `/union war end [tag]` | Proposes ending a war \(the other union accepts\) |
| `/union alliances` | Lists all unions and their allies |
| `/union rivalries` | Lists all unions and their rivals |

## Permissions

| Permission | Description |
| :--- | :--- |
| `unionsog.member.ally` | Can use ally chat |
| `unionsog.member.ally-set` | Can add and remove their union's allies |
| `unionsog.member.rival` | Can start and end a rivalry with another union |
| `unionsog.member.war` | Can propose a war and answer war requests |
| `unionsog.anyone.alliances` | Can view alliances by union |
| `unionsog.anyone.rivalries` | Can view rivalries by union |
