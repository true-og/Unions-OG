package net.trueog.unionsog;

/**
 * Requests that are put to somebody else and resolved by whoever answers.
 * <p>
 * {@link #CREATE_ALLY}, {@link #BREAK_RIVALRY} and {@link #END_WAR} are sent to
 * another union and settled by the first member of that union to answer.
 * {@link #INVITE} and {@link #REGROUP} are put to one player, who answers only
 * for themselves.
 * </p>
 * <p>
 * Decisions that only concern a union's own members are not requests, they are
 * {@link Proposal}s and need a consensus of that union's members.
 * </p>
 *
 * @author phaed
 */
public enum UnionRequest {

    INVITE, CREATE_ALLY, BREAK_RIVALRY, END_WAR, REGROUP;

    /**
     * @return whether the request is put to one player rather than to a union
     */
    public boolean isAddressedToPlayer() {

        return this == INVITE || this == REGROUP;

    }

}
