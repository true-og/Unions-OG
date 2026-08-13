package net.trueog.unionsog.managers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.trueog.unionsog.*;
import net.trueog.unionsog.events.RequestEvent;
import net.trueog.unionsog.events.RequestFinishedEvent;
import net.trueog.unionsog.events.WarEndEvent;
import net.trueog.unionsog.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.*;

import static net.trueog.unionsog.UnionsOG.lang;
import static net.trueog.unionsog.managers.SettingsManager.ConfigField.*;
import static org.bukkit.ChatColor.RED;

/**
 * Handles the requests a union sends to <i>another</i> union, plus membership
 * invitations. The first member of the asked union to answer decides the
 * outcome.
 * <p>
 * Decisions that only concern a union's own members go through the
 * {@link ProposalManager} instead, where they need a consensus of that union's
 * members.
 * </p>
 *
 * @author phaed
 */
public final class RequestManager {

    private final UnionsOG plugin;
    private final HashMap<String, Request> requests = new HashMap<>();

    /**
     *
     */
    public RequestManager() {

        plugin = UnionsOG.getInstance();
        askerTask();

    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean hasRequest(String tag) {

        return requests.containsKey(tag);

    }

    /**
     * Add a member invite request
     *
     * @param requester   the requester
     * @param invitedName the invited Player
     * @param union       the Union
     */
    public void addInviteRequest(UnionPlayer requester, String invitedName, Union union) {

        if (requests.containsKey(invitedName.toLowerCase())) {

            return;

        }

        Player player = Bukkit.getPlayer(invitedName);
        if (player == null) {

            return;

        }

        String msg = lang("inviting.you.to.join", player, requester.getName(), union.getName());
        Request req = new Request(UnionRequest.INVITE, null, requester, invitedName, union, msg);
        requests.put(invitedName.toLowerCase(), req);
        ask(req);

    }

    /**
     * Asks one member whether they want to be teleported, so that a regroup moves
     * only the members who agree to it.
     *
     * @param requester   the member asking for the regroup
     * @param member      the member being asked
     * @param union       the Union
     * @param destination where the member would end up
     * @return whether the member was asked
     */
    public boolean addRegroupRequest(@NotNull UnionPlayer requester, @NotNull UnionPlayer member, @NotNull Union union,
            @NotNull Location destination)
    {

        Player player = member.toPlayer();
        if (player == null) {

            return false;

        }

        // One pending question per player, the same as an invitation.
        if (requests.containsKey(member.getCleanName())) {

            return false;

        }

        String msg = lang("asking.to.teleport.you", player, requester.getName());
        Request req = new Request(UnionRequest.REGROUP, null, requester, player.getName(), union, msg);
        req.setDestination(destination);
        requests.put(member.getCleanName(), req);
        ask(req);

        return true;

    }

    public void addWarEndRequest(UnionPlayer requester, Union warUnion, Union requestingUnion) {

        if (requests.containsKey(warUnion.getTag())) {

            return;

        }

        String msg = MessageFormat.format(lang("proposing.to.end.the.war"), requestingUnion.getName(),
                ChatUtils.stripColors(warUnion.getColorTag()));

        List<UnionPlayer> acceptors = Helper.stripOffLinePlayers(warUnion.getMembers());
        acceptors.remove(requester);

        Request req = new Request(UnionRequest.END_WAR, acceptors, requester, warUnion.getTag(), requestingUnion, msg);
        requests.put(req.getTarget(), req);
        ask(req);

    }

    public void addAllyRequest(UnionPlayer requester, Union allyUnion, Union requestingUnion) {

        if (requests.containsKey(allyUnion.getTag())) {

            return;

        }

        String msg = MessageFormat.format(lang("proposing.an.alliance"), requestingUnion.getName(),
                ChatUtils.stripColors(allyUnion.getColorTag()));

        List<UnionPlayer> acceptors = Helper.stripOffLinePlayers(allyUnion.getMembers());
        acceptors.remove(requester);

        Request req = new Request(UnionRequest.CREATE_ALLY, acceptors, requester, allyUnion.getTag(), requestingUnion,
                msg);
        requests.put(req.getTarget(), req);
        ask(req);

    }

    public void addRivalryBreakRequest(UnionPlayer requester, Union rivalUnion, Union requestingUnion) {

        if (requests.containsKey(rivalUnion.getTag())) {

            return;

        }

        String msg = MessageFormat.format(lang("proposing.to.end.the.rivalry"), requestingUnion.getName(),
                ChatUtils.stripColors(rivalUnion.getColorTag()));

        List<UnionPlayer> acceptors = Helper.stripOffLinePlayers(rivalUnion.getMembers());
        acceptors.remove(requester);

        Request req = new Request(UnionRequest.BREAK_RIVALRY, acceptors, requester, rivalUnion.getTag(),
                requestingUnion, msg);
        requests.put(req.getTarget(), req);
        ask(req);

    }

    public void accept(UnionPlayer cp) {

        Request req = requests.get(cp.getTag());

        if (req != null) {

            req.vote(cp.getName(), VoteResult.ACCEPT);
            processResults(req);

        } else {

            req = requests.get(cp.getCleanName());

            if (req != null) {

                answer(req, VoteResult.ACCEPT);

            }

        }

    }

    public void deny(UnionPlayer cp) {

        Request req = requests.get(cp.getTag());

        if (req != null) {

            req.vote(cp.getName(), VoteResult.DENY);
            processResults(req);

        } else {

            req = requests.get(cp.getCleanName());

            if (req != null) {

                answer(req, VoteResult.DENY);

            }

        }

    }

    /**
     * Settles a request that was put to one player, who answers only for
     * themselves.
     *
     * @param req  the Request
     * @param vote their answer
     */
    private void answer(@NotNull Request req, @NotNull VoteResult vote) {

        if (req.getType() == UnionRequest.REGROUP) {

            processRegroup(req, vote);

        } else {

            processInvite(req, vote);

        }

    }

    /**
     * Teleports a member who agreed to be regrouped, and tells the member who asked
     * either way.
     *
     * @param req  the Request
     * @param vote the answer
     */
    private void processRegroup(@NotNull Request req, @NotNull VoteResult vote) {

        requests.remove(req.getTarget().toLowerCase());

        Player member = Bukkit.getPlayerExact(req.getTarget());
        Location destination = req.getDestination();
        if (member == null || destination == null) {

            return;

        }

        Player requester = req.getRequester().toPlayer();

        if (vote.equals(VoteResult.ACCEPT)) {

            ChatBlock.sendMessageKey(member, "accepted.the.teleport", req.getRequester().getName());
            if (requester != null) {

                ChatBlock.sendMessageKey(requester, "accepted.your.teleport", member.getName());

            }

            plugin.getTeleportManager().queueRegroup(member, req.getUnion(), destination);

        } else {

            ChatBlock.sendMessageKey(member, "denied.the.teleport", req.getRequester().getName());
            if (requester != null) {

                ChatBlock.sendMessageKey(requester, "denied.your.teleport", member.getName());

            }

        }

    }

    public void processInvite(Request req, VoteResult vote) {

        requests.remove(req.getTarget().toLowerCase());

        Union union = req.getUnion();
        Player invited = Bukkit.getPlayerExact(req.getTarget());
        if (invited == null) {

            return;

        }

        if (vote.equals(VoteResult.ACCEPT)) {

            UnionPlayer cp = plugin.getUnionManager().getCreateUnionPlayer(invited.getUniqueId());
            int maxMembers = plugin.getSettingsManager().getInt(UNION_MAX_MEMBERS);

            if (maxMembers > 0 && maxMembers > union.getSize()) {

                ChatBlock.sendMessageKey(invited, "accepted.invitation", union.getName());
                union.addBb(lang("joined.the.union", invited.getName()));
                plugin.getUnionManager().serverAnnounce(lang("has.joined", invited.getName(), union.getName()));
                union.addPlayerToUnion(cp);

            } else {

                ChatBlock.sendMessageKey(invited, "this.union.has.reached.the.member.limit");

            }

        } else {

            ChatBlock.sendMessageKey(invited, "denied.invitation", union.getName());
            union.memberAnnounce(RED + lang("membership.invitation", invited.getName()));

        }

    }

    public void processResults(Request req) {

        Union requestUnion = req.getUnion();
        UnionPlayer requester = req.getRequester();

        String target = req.getTarget();

        @Nullable
        Union targetUnion = plugin.getUnionManager().getUnion(target);

        List<String> accepts = req.getAccepts();
        List<String> denies = req.getDenies();

        switch (req.getType()) {

            case END_WAR:
                processEndWar(requester, requestUnion, targetUnion, accepts, denies);
                break;
            case CREATE_ALLY:
                processCreateAlly(requester, requestUnion, targetUnion, accepts, denies);
                break;
            case BREAK_RIVALRY:
                processBreakRivalry(requester, requestUnion, targetUnion, accepts, denies);
                break;
            default:
                return;

        }

        requests.remove(target);
        UnionsOG.getInstance().getServer().getPluginManager().callEvent(new RequestFinishedEvent(req));
        req.cleanVotes();

    }

    private void processBreakRivalry(UnionPlayer requester, Union requestUnion, @Nullable Union targetUnion,
            List<String> accepts, List<String> denies)
    {

        if (targetUnion != null && requestUnion != null) {

            if (!accepts.isEmpty()) {

                requestUnion.removeRival(targetUnion);
                targetUnion.addBb(requester.getName(),
                        lang("broken.the.rivalry", accepts.get(0), requestUnion.getName()));
                requestUnion.addBb(requester.getName(),
                        lang("broken.the.rivalry.with", requester.getName(), targetUnion.getName()));

            } else {

                targetUnion.addBb(requester.getName(),
                        lang("denied.to.make.peace", denies.get(0), requestUnion.getName()));
                requestUnion.addBb(requester.getName(), lang("peace.agreement.denied", targetUnion.getName()));

            }

        }

    }

    private void processCreateAlly(UnionPlayer requester, Union requestUnion, @Nullable Union targetUnion,
            List<String> accepts, List<String> denies)
    {

        if (targetUnion != null && requestUnion != null) {

            if (!accepts.isEmpty()) {

                requestUnion.addAlly(targetUnion);

                targetUnion.addBb(requester.getName(),
                        lang("accepted.an.alliance", accepts.get(0), requestUnion.getName()));
                requestUnion.addBb(requester.getName(),
                        lang("created.an.alliance", requester.getName(), targetUnion.getName()));

            } else {

                targetUnion.addBb(requester.getName(),
                        lang("denied.an.alliance", denies.get(0), requestUnion.getName()));
                requestUnion.addBb(requester.getName(), lang("the.alliance.was.denied", targetUnion.getName()));

            }

        }

    }

    private void processEndWar(UnionPlayer requester, Union requestUnion, @Nullable Union targetUnion,
            List<String> accepts, List<String> denies)
    {

        if (requestUnion != null && targetUnion != null) {

            if (!accepts.isEmpty()) {

                War war = plugin.getProtectionManager().getWar(requestUnion, targetUnion);
                plugin.getProtectionManager().removeWar(war, WarEndEvent.Reason.REQUEST);
                requestUnion.removeWarringUnion(targetUnion);
                targetUnion.removeWarringUnion(requestUnion);

                targetUnion.addBb(requester.getName(),
                        lang("you.are.no.longer.at.war", accepts.get(0), requestUnion.getColorTag()));
                requestUnion.addBb(requester.getName(),
                        lang("you.are.no.longer.at.war", requestUnion.getName(), targetUnion.getColorTag()));

            } else {

                targetUnion.addBb(requester.getName(), lang("denied.war.end", denies.get(0), requestUnion.getName()));
                requestUnion.addBb(requester.getName(), lang("end.war.denied", targetUnion.getName()));

            }

        }

    }

    /**
     * End a pending request prematurely
     *
     * @param playerName the Player signing off
     */
    public void endPendingRequest(String playerName) {

        for (Request req : new LinkedList<>(requests.values())) {

            for (UnionPlayer cp : req.getAcceptors()) {

                if (cp.getName().equalsIgnoreCase(playerName)) {

                    req.getUnion()
                            .memberAnnounce(lang("signed.off.request.cancelled", RED + playerName, req.getType()));
                    requests.remove(req.getUnion().getTag());
                    break;

                }

            }

        }

    }

    public void removeRequest(@NotNull String keyOrTarget) {

        Iterator<Map.Entry<String, Request>> iterator = requests.entrySet().iterator();
        while (iterator.hasNext()) {

            Map.Entry<String, Request> entry = iterator.next();
            final String requester = entry.getKey();
            final String target = entry.getValue().getTarget();
            if (keyOrTarget.equals(requester) || keyOrTarget.equals(target)) {

                entry.getValue().cleanVotes();
                iterator.remove();

            }

        }

    }

    /**
     * Starts the task that asks for the votes of all requests
     */
    public void askerTask() {

        new BukkitRunnable() {

            @Override
            public void run() {

                for (Iterator<Map.Entry<String, Request>> iter = requests.entrySet().iterator(); iter.hasNext();) {

                    Request req = iter.next().getValue();

                    if (req == null) {

                        continue;

                    }

                    if (req.reachedRequestLimit()) {

                        iter.remove();

                    }

                    ask(req);
                    req.incrementAskCount();

                }

            }

        }.runTaskTimerAsynchronously(plugin, 0, plugin.getSettingsManager().getSeconds(REQUEST_FREQUENCY));

    }

    /**
     * Asks a request to players for votes
     *
     * @param req the Request
     */
    public void ask(final Request req) {

        String message = lang("request.message", req.getUnion().getColorTag(), req.getMsg());
        ArrayList<Player> recipients = new ArrayList<>();
        if (req.getType().isAddressedToPlayer()) {

            recipients.add(Bukkit.getPlayerExact(req.getTarget()));

        } else {

            for (UnionPlayer cp : req.getAcceptors()) {

                if (cp.getVote() == null) {

                    recipients.add(cp.toPlayer());

                }

            }

        }

        for (Player recipient : recipients) {

            if (recipient != null) {

                sendRequestMessage(recipient, message, req);

            }

        }

        Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getPluginManager().callEvent(new RequestEvent(req)));

    }

    /**
     * Sends a request message with clickable accept/deny buttons using MiniMessage
     *
     * @param player  the player to send the message to
     * @param message the request message
     * @param req     the request (to determine which command to run)
     */
    private void sendRequestMessage(Player player, String message, Request req) {

        MiniMessage miniMessage = MiniMessage.miniMessage();
        String acceptCommand = plugin.getSettingsManager().getString(COMMANDS_ACCEPT);
        String denyCommand = plugin.getSettingsManager().getString(COMMANDS_DENY);

        // Create the clickable buttons using MiniMessage
        Component requestComponent = miniMessage.deserialize(message).append(Component.newline())
                .append(Component.text("       "))
                .append(Component.text("[ACCEPT]").color(net.kyori.adventure.text.format.NamedTextColor.GREEN)
                        .decorate(net.kyori.adventure.text.format.TextDecoration.BOLD)
                        .clickEvent(ClickEvent.runCommand("/" + acceptCommand))
                        .hoverEvent(net.kyori.adventure.text.event.HoverEvent
                                .showText(miniMessage.deserialize("<green>Click here to accept</green>"))))
                .append(Component.text(" or "))
                .append(Component.text("[DENY]").color(net.kyori.adventure.text.format.NamedTextColor.RED)
                        .decorate(net.kyori.adventure.text.format.TextDecoration.BOLD)
                        .clickEvent(ClickEvent.runCommand("/" + denyCommand))
                        .hoverEvent(net.kyori.adventure.text.event.HoverEvent
                                .showText(miniMessage.deserialize("<red>Click here to deny</red>"))))
                .append(Component.newline());

        player.sendMessage(requestComponent);

    }

}
