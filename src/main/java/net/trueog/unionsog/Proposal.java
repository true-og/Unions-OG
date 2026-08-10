package net.trueog.unionsog;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * A decision put to a union's own members.
 * <p>
 * A proposal passes as soon as half of the union's members have voted yes, and
 * is rejected as soon as enough members have voted no that half can no longer
 * be reached. Votes survive logouts and restarts, so members can answer the
 * next time they log in.
 * </p>
 */
public final class Proposal {

    private final @NotNull ProposalType type;
    private final @NotNull String unionTag;
    private final @NotNull UUID proposer;
    private final @NotNull String target;
    private final long createdAt;
    private final Map<UUID, Boolean> votes = new LinkedHashMap<>();

    public Proposal(@NotNull ProposalType type, @NotNull String unionTag, @NotNull UUID proposer,
            @NotNull String target, long createdAt)
    {

        this.type = type;
        this.unionTag = unionTag;
        this.proposer = proposer;
        this.target = target;
        this.createdAt = createdAt;

    }

    public @NotNull ProposalType getType() {

        return type;

    }

    public @NotNull String getUnionTag() {

        return unionTag;

    }

    public @NotNull UUID getProposer() {

        return proposer;

    }

    /**
     * @return the proposal's payload, empty for proposals that do not need one
     */
    public @NotNull String getTarget() {

        return target;

    }

    public long getCreatedAt() {

        return createdAt;

    }

    public @NotNull Map<UUID, Boolean> getVotes() {

        return Collections.unmodifiableMap(votes);

    }

    public void putVote(@NotNull UUID voter, boolean inFavour) {

        votes.put(voter, inFavour);

    }

    public void putVotes(@NotNull Map<UUID, Boolean> votes) {

        this.votes.putAll(votes);

    }

    public @Nullable Boolean getVote(@NotNull UUID voter) {

        return votes.get(voter);

    }

    public boolean hasVoted(@NotNull UUID voter) {

        return votes.containsKey(voter);

    }

    /**
     * Drops the votes of players who are no longer members of the union, so that
     * someone leaving cannot keep holding a proposal open.
     *
     * @param members the current members
     */
    public void retainVoters(@NotNull Iterable<UnionPlayer> members) {

        Set<UUID> current = new HashSet<>();
        for (UnionPlayer cp : members) {

            current.add(cp.getUniqueId());

        }

        votes.keySet().retainAll(current);

    }

    public int countVotes(boolean inFavour) {

        int count = 0;
        for (Boolean vote : votes.values()) {

            if (vote == inFavour) {

                count++;

            }

        }

        return count;

    }

    /**
     * The number of yes votes needed, which is half of the union rounded up.
     *
     * @param memberCount the union's member count
     */
    public static int votesNeeded(int memberCount) {

        return (memberCount + 1) / 2;

    }

    /**
     * @param memberCount the union's member count
     * @return whether enough members are in favour
     */
    public boolean isPassed(int memberCount) {

        return countVotes(true) >= votesNeeded(memberCount);

    }

    /**
     * @param memberCount the union's member count
     * @return whether so many members are against that it can no longer pass
     */
    public boolean isRejected(int memberCount) {

        return countVotes(false) > memberCount - votesNeeded(memberCount);

    }

    public boolean isExpired(long now, long lifetimeMillis) {

        return now - createdAt >= lifetimeMillis;

    }

}
