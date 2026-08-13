package net.trueog.unionsog;

/**
 * The decisions a union's own members vote on.
 *
 * @see Proposal
 */
public enum ProposalType {

    /**
     * Disband the union. The target is unused.
     */
    DISBAND,
    /**
     * Move the union's home base. The target is the serialized location.
     */
    SET_HOME,
    /**
     * Declare war on another union. The target is that union's tag.
     */
    START_WAR,
    /**
     * Rename the union. The target is the new name.
     */
    RENAME;

    public static @org.jetbrains.annotations.Nullable ProposalType fromName(
            @org.jetbrains.annotations.Nullable String name)
    {

        if (name == null) {

            return null;

        }

        try {

            return valueOf(name);

        } catch (IllegalArgumentException ex) {

            return null;

        }

    }

}
