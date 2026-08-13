package net.trueog.unionsog.managers;

import net.trueog.unionsog.*;
import net.trueog.unionsog.ui.frames.ProposalFrame;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static net.trueog.unionsog.UnionsOG.lang;
import static net.trueog.unionsog.managers.SettingsManager.ConfigField.ENABLE_GUI;
import static org.bukkit.ChatColor.AQUA;
import static org.bukkit.ChatColor.RED;

/**
 * Handles the decisions a union's own members vote on.
 * <p>
 * A union can only have one proposal open at a time. It passes as soon as half
 * of the union's members are in favour and is rejected as soon as half can no
 * longer be reached. Votes are stored in the database, so a member who is
 * offline when a proposal opens can still vote the next time they log in.
 * </p>
 */
public final class ProposalManager {

    /**
     * How long a proposal stays open before it is dropped
     */
    public static final long LIFETIME_MILLIS = TimeUnit.DAYS.toMillis(7);

    private static final long CHECK_INTERVAL_TICKS = 20L * 60L * 5L;
    private static final long JOIN_PROMPT_DELAY_TICKS = 20L;

    private final UnionsOG plugin;
    private final Map<String, Proposal> proposals = new HashMap<>();
    /** Members already shown a given proposal, so it is only presented once. */
    private final Set<String> presented = new HashSet<>();

    public ProposalManager() {

        plugin = UnionsOG.getInstance();
        proposals.putAll(plugin.getStorageManager().retrieveProposals());
        expiryTask();

    }

    public @Nullable Proposal getProposal(@Nullable Union union) {

        return union == null ? null : proposals.get(union.getTag());

    }

    /**
     * Opens a proposal and casts the proposer's vote in favour.
     *
     * @param proposer the member opening the proposal
     * @param union    the union voting on it
     * @param type     what is being proposed
     * @param target   the proposal's payload, empty when it does not need one
     * @return whether the proposal was opened
     */
    public boolean propose(@NotNull UnionPlayer proposer, @NotNull Union union, @NotNull ProposalType type,
            @NotNull String target)
    {

        if (proposals.containsKey(union.getTag())) {

            ChatBlock.sendMessage(proposer, RED + lang("proposal.already.open", proposer));
            return false;

        }

        Proposal proposal = new Proposal(type, union.getTag(), proposer.getUniqueId(), target,
                System.currentTimeMillis());
        proposal.putVote(proposer.getUniqueId(), true);
        proposals.put(union.getTag(), proposal);

        union.memberAnnounce(AQUA + lang("proposal.opened", proposer.getName(), describe(proposal, union)));
        union.memberAnnounce(AQUA + lang("proposal.how.to.vote"));

        return tally(union, proposal);

    }

    /**
     * Records a member's vote on their union's open proposal.
     *
     * @param voter    the voting member
     * @param union    their union
     * @param inFavour whether they are in favour
     */
    public void vote(@NotNull UnionPlayer voter, @NotNull Union union, boolean inFavour) {

        Proposal proposal = proposals.get(union.getTag());
        if (proposal == null) {

            ChatBlock.sendMessage(voter, RED + lang("proposal.none.open", voter));
            return;

        }

        proposal.putVote(voter.getUniqueId(), inFavour);
        ChatBlock.sendMessage(voter, AQUA + lang(inFavour ? "proposal.voted.yes" : "proposal.voted.no", voter));

        tally(union, proposal);

    }

    /**
     * Tells a member about the proposal their union has open, if any.
     *
     * @param player the member logging in
     */
    public void remind(@NotNull Player player) {

        UnionPlayer cp = plugin.getUnionManager().getUnionPlayer(player);
        Union union = cp != null ? cp.getUnion() : null;
        Proposal proposal = getProposal(union);
        if (cp == null || union == null || proposal == null || proposal.hasVoted(cp.getUniqueId())) {

            return;

        }

        if (!presented.add(key(union.getTag(), cp.getUniqueId()))) {

            return;

        }

        ChatBlock.sendMessage(player, AQUA + lang("proposal.pending", player, describe(proposal, union)));

        if (!plugin.getSettingsManager().is(ENABLE_GUI)) {

            ChatBlock.sendMessage(player, AQUA + lang("proposal.how.to.vote", player));
            return;

        }

        // The inventory cannot be opened while the join is still being handled.
        Bukkit.getScheduler().runTaskLater(plugin, () -> {

            if (player.isOnline() && !ProposalFrame.openIfPending(player)) {

                ChatBlock.sendMessage(player, AQUA + lang("proposal.how.to.vote", player));

            }

        }, JOIN_PROMPT_DELAY_TICKS);

    }

    private static String key(@NotNull String unionTag, @NotNull UUID member) {

        return unionTag + "/" + member;

    }

    /**
     * @return a human readable summary of the proposal and the votes so far
     */
    public String describe(@NotNull Proposal proposal, @NotNull Union union) {

        String subject;
        switch (proposal.getType()) {

            case SET_HOME:
                subject = lang("proposal.description.set.home");
                break;
            case START_WAR:
                Union target = plugin.getUnionManager().getUnion(proposal.getTarget());
                subject = lang("proposal.description.start.war",
                        target != null ? target.getName() : proposal.getTarget());
                break;
            case RENAME:
                subject = lang("proposal.description.rename", proposal.getTarget());
                break;
            case DISBAND:
            default:
                subject = lang("proposal.description.disband");

        }

        return lang("proposal.summary", subject, proposal.countVotes(true), Proposal.votesNeeded(union.getSize()),
                proposal.countVotes(false));

    }

    /**
     * Counts the votes and applies, rejects or keeps the proposal open.
     *
     * @return whether the proposal is still open
     */
    private boolean tally(@NotNull Union union, @NotNull Proposal proposal) {

        proposal.retainVoters(union.getMembers());
        int members = union.getSize();

        if (proposal.isPassed(members)) {

            close(union, proposal);
            apply(union, proposal);
            return false;

        }

        if (proposal.isRejected(members)) {

            close(union, proposal);
            union.memberAnnounce(RED + lang("proposal.rejected", describe(proposal, union)));
            return false;

        }

        plugin.getStorageManager().saveProposal(proposal);
        return true;

    }

    private void close(@NotNull Union union, @NotNull Proposal proposal) {

        proposals.remove(union.getTag());
        presented.removeIf(k -> k.startsWith(proposal.getUnionTag() + "/"));
        plugin.getStorageManager().deleteProposal(proposal.getUnionTag());

    }

    /**
     * Drops a union's open proposal without resolving it, used when the union
     * itself goes away.
     */
    public void remove(@NotNull String unionTag) {

        if (proposals.remove(unionTag) != null) {

            presented.removeIf(k -> k.startsWith(unionTag + "/"));
            plugin.getStorageManager().deleteProposal(unionTag);

        }

    }

    private void apply(@NotNull Union union, @NotNull Proposal proposal) {

        UnionPlayer proposer = plugin.getUnionManager().getAnyUnionPlayer(proposal.getProposer());

        switch (proposal.getType()) {

            case SET_HOME: {

                Location home = parseLocation(proposal.getTarget());
                if (home == null) {

                    union.memberAnnounce(RED + lang("proposal.home.world.missing"));
                    return;

                }

                Player proposerPlayer = proposer != null ? proposer.toPlayer() : null;
                if (proposerPlayer != null && !plugin.getUnionManager().purchaseHomeTeleportSet(proposerPlayer)) {

                    return;

                }

                union.setHomeLocation(home);
                union.addBb(lang("union.home.moved", Helper.toLocationString(home)));
                break;

            }
            case START_WAR: {

                Union target = plugin.getUnionManager().getUnion(proposal.getTarget());
                if (target == null || proposer == null) {

                    union.memberAnnounce(RED + lang("proposal.war.target.missing"));
                    return;

                }

                plugin.getProtectionManager().addWar(proposer, union, target);
                break;

            }
            case RENAME: {

                union.addBb(proposer != null ? proposer.getName() : union.getName(),
                        lang("union.renamed.to.0", proposal.getTarget()));
                union.setName(proposal.getTarget());
                plugin.getStorageManager().updateUnion(union);
                break;

            }
            case DISBAND:
            default:
                union.disband(proposer != null ? proposer.toPlayer() : null, true, false);

        }

    }

    private void expiryTask() {

        new BukkitRunnable() {

            @Override
            public void run() {

                long now = System.currentTimeMillis();
                for (Proposal proposal : new ArrayList<>(proposals.values())) {

                    if (!proposal.isExpired(now, LIFETIME_MILLIS)) {

                        continue;

                    }

                    Union union = plugin.getUnionManager().getUnion(proposal.getUnionTag());
                    remove(proposal.getUnionTag());
                    if (union != null) {

                        union.memberAnnounce(RED + lang("proposal.expired", describe(proposal, union)));

                    }

                }

            }

        }.runTaskTimer(plugin, CHECK_INTERVAL_TICKS, CHECK_INTERVAL_TICKS);

    }

    /**
     * Serializes a location so it can survive in the database until the vote
     * finishes.
     */
    public static String toLocationTarget(@NotNull Location location) {

        World world = location.getWorld();
        return (world != null ? world.getName() : "") + ";" + location.getX() + ";" + location.getY() + ";"
                + location.getZ() + ";" + location.getYaw() + ";" + location.getPitch();

    }

    private static @Nullable Location parseLocation(@NotNull String target) {

        String[] parts = target.split(";");
        if (parts.length != 6) {

            return null;

        }

        World world = Bukkit.getWorld(parts[0]);
        if (world == null) {

            return null;

        }

        try {

            return new Location(world, Double.parseDouble(parts[1]), Double.parseDouble(parts[2]),
                    Double.parseDouble(parts[3]), Float.parseFloat(parts[4]), Float.parseFloat(parts[5]));

        } catch (NumberFormatException ex) {

            return null;

        }

    }

}
