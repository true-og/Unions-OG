package net.trueog.unionsog;

import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static net.trueog.unionsog.managers.SettingsManager.ConfigField.REQUEST_MAX;

/**
 * @author phaed
 */
public final class Request {

    private List<UnionPlayer> acceptors = new ArrayList<>();
    private Union union;
    private String msg;
    private String target;
    private UnionRequest type;
    private UnionPlayer requester;
    private int askCount;
    private Location destination;

    public Request(UnionRequest type, @Nullable List<UnionPlayer> acceptors, UnionPlayer requester, String target,
            Union union, String msg)
    {

        this.type = type;
        this.target = target;
        this.union = union;
        this.msg = msg;
        if (acceptors != null) {

            this.acceptors = acceptors;

        }

        this.requester = requester;

        cleanVotes();

    }

    /**
     * Where a {@link UnionRequest#REGROUP} would put the player it is asking, so
     * that the teleport can wait for their answer.
     *
     * @return the destination, null for requests that do not teleport anybody
     */
    public @Nullable Location getDestination() {

        return destination;

    }

    /**
     * @param destination the destination to set
     */
    public void setDestination(@Nullable Location destination) {

        this.destination = destination;

    }

    /**
     * @return the type
     */
    public UnionRequest getType() {

        return type;

    }

    /**
     * @param type the type to set
     */
    public void setType(UnionRequest type) {

        this.type = type;

    }

    /**
     * @return the acceptors
     */
    public List<UnionPlayer> getAcceptors() {

        return Collections.unmodifiableList(acceptors);

    }

    /**
     * @param acceptors the acceptors to set
     */
    public void setAcceptors(List<UnionPlayer> acceptors) {

        this.acceptors = acceptors;

    }

    /**
     * @return the union
     */
    public Union getUnion() {

        return union;

    }

    /**
     * @param union the union to set
     */
    public void setUnion(Union union) {

        this.union = union;

    }

    /**
     * @return the msg
     */
    public String getMsg() {

        return msg;

    }

    /**
     * @param msg the msg to set
     */
    public void setMsg(String msg) {

        this.msg = msg;

    }

    /**
     * @return the target
     */
    public String getTarget() {

        return target;

    }

    /**
     * @param target the target to set
     */
    public void setTarget(String target) {

        this.target = target;

    }

    public void vote(String playerName, VoteResult vote) {

        for (UnionPlayer cp : acceptors) {

            if (cp.getName().equalsIgnoreCase(playerName)) {

                cp.setVote(vote);

            }

        }

    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean votingFinished() {

        for (UnionPlayer cp : acceptors) {

            if (cp.getVote() == null) {

                return false;

            }

        }

        return true;

    }

    public List<String> getDenies() {

        List<String> out = new ArrayList<>();

        for (UnionPlayer cp : acceptors) {

            if (cp.getVote() != null && cp.getVote().equals(VoteResult.DENY)) {

                out.add(cp.getName());

            }

        }

        return out;

    }

    public List<String> getAccepts() {

        List<String> out = new ArrayList<>();

        for (UnionPlayer cp : acceptors) {

            if (cp.getVote() != null && cp.getVote().equals(VoteResult.ACCEPT)) {

                out.add(cp.getName());

            }

        }

        return out;

    }

    /**
     * Cleans votes
     */
    public void cleanVotes() {

        for (UnionPlayer cp : acceptors) {

            cp.setVote(null);

        }

    }

    /**
     * @return the requester
     */
    public UnionPlayer getRequester() {

        return requester;

    }

    /**
     * @param requester the requester to set
     */
    public void setRequester(UnionPlayer requester) {

        this.requester = requester;

    }

    public void incrementAskCount() {

        askCount += 1;

    }

    public boolean reachedRequestLimit() {

        return askCount > UnionsOG.getInstance().getSettingsManager().getInt(REQUEST_MAX);

    }

}
