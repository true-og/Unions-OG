package net.trueog.unionsog;

/**
 * Requests that are sent to another union and resolved by the first member of
 * that union who answers.
 * <p>
 * Decisions that only concern a union's own members are not requests, they are
 * {@link Proposal}s and need a consensus of that union's members.
 * </p>
 *
 * @author phaed
 */
public enum UnionRequest {
    INVITE, CREATE_ALLY, BREAK_RIVALRY, END_WAR
}
