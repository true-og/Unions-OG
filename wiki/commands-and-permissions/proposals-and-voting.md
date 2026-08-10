---
description: How unions decide things that need the whole membership to agree.
---

# Proposals and Voting

Unions in Unions-OG are flat: there are no leaders and no ranks, so nobody can
make a decision on the union's behalf. Three actions are important enough that
they need the membership's consent instead, and those open a **proposal**.

| Action | Command that opens the proposal |
| :--- | :--- |
| Disbanding the union | `/union disband` |
| Moving the union's home | `/union home set` |
| Declaring war on a rival | `/union war start (tag)` |

Everything else a union can do is available to any member immediately.

## How a proposal is decided

- The member who opens a proposal automatically votes in favour.
- A proposal **passes** as soon as half the union's members, rounded up, have
  voted yes. In a five-member union that is three votes; in a six-member union
  it is also three.
- A proposal is **rejected** as soon as so many members have voted no that the
  yes threshold can no longer be reached. There is no need to wait for the
  remaining votes.
- A proposal **expires** after 7 days if neither side reaches its threshold.
- A union can only have one proposal open at a time. The next one cannot be
  opened until the current one finishes.
- Members who leave the union have their votes discarded, so nobody can hold a
  proposal open by resigning.

Because a passing proposal takes effect immediately, a single-member union
disbands as soon as that member confirms.

## Voting

Votes are stored in the database, so members do **not** have to be online when
a proposal opens. A member who is away can vote the next time they log in, and
the proposal waits for them until it expires.

| Command | Description |
| :--- | :--- |
| `/union vote` | Shows the open proposal, what it would do, and the running tally |
| `/union vote yes` | Votes in favour |
| `/union vote no` | Votes against |

Members can also vote from the GUI. Open `/union`, click the **Union vote**
book, and choose the green or red wool. The vote screen is presented once
automatically when a member logs in with a proposal they have not voted on
yet; it is not shown again on later logins, so the GUI button or the command
is how to come back to it.

## Permissions

| Permission | Description |
| :--- | :--- |
| `unionsog.member.vote` | Can vote on the union's proposals |
| `unionsog.member.disband` | Can propose disbanding the union |
| `unionsog.member.home-set` | Can propose moving the union's home |
| `unionsog.member.war` | Can propose a war and answer war requests |

All four are granted by default through `unionsog.member.*`.

## Staff override

`/union mod disband (union)` disbands a union immediately, without a vote, for
anyone holding `unionsog.mod.disband`.
