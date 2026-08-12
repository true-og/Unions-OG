package net.trueog.unionsog.commands.union;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import net.trueog.unionsog.*;
import net.trueog.unionsog.commands.UnionInput;
import net.trueog.unionsog.commands.UnionPlayerInput;
import net.trueog.unionsog.conversation.DisbandPrompt;
import net.trueog.unionsog.conversation.ResignPrompt;
import net.trueog.unionsog.conversation.SCConversation;
import net.trueog.unionsog.hooks.discord.DiscordHook;
import net.trueog.unionsog.hooks.discord.exceptions.DiscordHookException;
import net.trueog.unionsog.events.TagChangeEvent;
import net.trueog.unionsog.managers.*;
import net.trueog.unionsog.utils.ChatUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static net.trueog.unionsog.UnionsOG.lang;
import static net.trueog.unionsog.managers.SettingsManager.ConfigField.*;
import static org.bukkit.ChatColor.AQUA;
import static org.bukkit.ChatColor.RED;

@CommandAlias("%union")
@Conditions("%basic_conditions")
public class UnionCommands extends BaseCommand {

    private static final Map<String, String> UNION_COLORS = new LinkedHashMap<>();

    static {

        UNION_COLORS.put("black", "&0");
        UNION_COLORS.put("dark_blue", "&1");
        UNION_COLORS.put("dark_green", "&2");
        UNION_COLORS.put("dark_aqua", "&3");
        UNION_COLORS.put("dark_red", "&4");
        UNION_COLORS.put("dark_purple", "&5");
        UNION_COLORS.put("gold", "&6");
        UNION_COLORS.put("gray", "&7");
        UNION_COLORS.put("dark_gray", "&8");
        UNION_COLORS.put("blue", "&9");
        UNION_COLORS.put("green", "&a");
        UNION_COLORS.put("aqua", "&b");
        UNION_COLORS.put("red", "&c");
        UNION_COLORS.put("light_purple", "&d");
        UNION_COLORS.put("yellow", "&e");
        UNION_COLORS.put("white", "&f");

    }

    @Dependency
    private UnionsOG plugin;
    @Dependency
    private SettingsManager settings;
    @Dependency
    private UnionManager cm;
    @Dependency
    private StorageManager storage;
    @Dependency
    private PermissionsManager permissions;
    @Dependency
    private RequestManager requestManager;
    @Dependency
    private ProposalManager proposalManager;
    @Dependency
    private ChatManager chatManager;

    @Subcommand("%disband")
    @CommandPermission("unionsog.member.disband")
    @Conditions("union_member")
    @Description("{@@command.description.disband}")
    public void disband(Player player) {

        new SCConversation(plugin, player, new DisbandPrompt()).begin();

    }

    @Subcommand("%vote")
    @CommandPermission("unionsog.member.vote")
    @Conditions("union_member")
    @Description("{@@command.description.vote}")
    public void voteStatus(Player player, Union union) {

        Proposal proposal = proposalManager.getProposal(union);
        if (proposal == null) {

            ChatBlock.sendMessage(player, RED + lang("proposal.none.open", player));
            return;

        }

        ChatBlock.sendMessage(player, AQUA + proposalManager.describe(proposal, union));
        ChatBlock.sendMessage(player, AQUA + lang("proposal.how.to.vote", player));

    }

    @Subcommand("%vote %yes")
    @CommandPermission("unionsog.member.vote")
    @Conditions("union_member")
    @Description("{@@command.description.vote.yes}")
    public void voteYes(UnionPlayer cp, Union union) {

        proposalManager.vote(cp, union, true);

    }

    @Subcommand("%vote %no")
    @CommandPermission("unionsog.member.vote")
    @Conditions("union_member")
    @Description("{@@command.description.vote.no}")
    public void voteNo(UnionPlayer cp, Union union) {

        proposalManager.vote(cp, union, false);

    }

    @Subcommand("%discord %create")
    @CommandPermission("unionsog.member.discord.create")
    @Conditions("union_member")
    @Description("{@@command.description.discord.create}")
    public void discord(Player player, Union union) {

        DiscordHook discordHook = chatManager.getDiscordHook();
        if (discordHook == null) {

            ChatBlock.sendMessageKey(player, "discordhook.is.disabled");
            return;

        }

        try {

            discordHook.createChannel(union.getTag());
            ChatBlock.sendMessageKey(player, "discord.created.successfully");

        } catch (DiscordHookException ex) {

            String messageKey = ex.getMessageKey();
            if (messageKey != null) {

                ChatBlock.sendMessage(player, RED + lang(messageKey));

            }

        }

    }

    @Subcommand("%war %start")
    @CommandPermission("unionsog.member.war")
    @Conditions("union_member")
    @Description("{@@command.description.war.start}")
    @CommandCompletion("@rivals")
    public void startWar(UnionPlayer requester, Union requestUnion,
            @Conditions("can_war_target") @Name("union") UnionInput targetUnionInput)
    {

        proposalManager.propose(requester, requestUnion, ProposalType.START_WAR, targetUnionInput.getUnion().getTag());

    }

    @Subcommand("%war %end")
    @CommandPermission("unionsog.member.war")
    @Conditions("union_member")
    @Description("{@@command.description.war.end}")
    @CommandCompletion("@warring_unions")
    public void endWar(UnionPlayer cp, Union issuerUnion, @Name("union") UnionInput other) {

        Union war = other.getUnion();
        if (issuerUnion.isWarring(war.getTag())) {

            requestManager.addWarEndRequest(cp, war, issuerUnion);
            ChatBlock.sendMessage(cp, AQUA + lang("leaders.asked.to.end.rivalry", cp, war.getName()));

        } else {

            ChatBlock.sendMessage(cp, RED + lang("unions.not.at.war", cp));

        }

    }

    @Subcommand("%color")
    @CommandPermission("unionsog.member.color")
    @Conditions("union_member")
    @CommandCompletion("@union_colors")
    @Description("{@@command.description.color}")
    public void color(Player player, Union union, @Name("color") String color) {

        Optional<String> tagColor = resolveTagColor(color);
        if (tagColor.isEmpty()) {

            ChatBlock.sendMessage(player, RED + lang("invalid.color", player));
            return;

        }

        String currentTag = ChatUtils.stripColors(union.getColorTag());
        String tag = tagColor.get() + currentTag;
        TagChangeEvent event = new TagChangeEvent(player, union, tag);
        plugin.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {

            return;

        }

        tag = event.getNewTag();
        String cleanTag = ChatUtils.stripColors(tag);

        Optional<String> validationError = plugin.getTagValidator().validate(player, tag, true);
        if (validationError.isPresent()) {

            ChatBlock.sendMessage(player, validationError.get());
            return;

        }

        if (!cleanTag.equals(currentTag)) {

            ChatBlock.sendMessage(player, RED + lang("you.can.only.modify.the.color.of.the.tag", player));
            return;

        }

        union.addBb(player.getName(), lang("tag.changed.to.0", ChatUtils.parseColors(tag)));
        union.changeUnionTag(tag);
        cm.updateDisplayName(player);
        ChatBlock.sendMessage(player, AQUA + lang("union.color.changed.to.0", player, ChatUtils.parseColors(tag)));

    }

    @Subcommand("%setbanner")
    @CommandPermission("unionsog.member.setbanner")
    @Conditions("union_member")
    @Description("{@@command.description.setbanner}")
    public void setbanner(Player player, Union union) {

        @SuppressWarnings("deprecation")
        ItemStack hand = player.getItemInHand();
        if (!hand.getType().toString().contains("BANNER")) {

            ChatBlock.sendMessageKey(player, "you.must.hold.a.banner");
            return;

        }

        union.setBanner(hand);
        storage.updateUnion(union);
        ChatBlock.sendMessageKey(player, "you.changed.union.banner");

    }

    private Optional<String> resolveTagColor(String input) {

        String normalized = input.toLowerCase(Locale.ROOT);
        if (UNION_COLORS.containsKey(normalized)) {

            return Optional.of(UNION_COLORS.get(normalized));

        }

        if (normalized.matches("#[0-9a-f]{6}")) {

            return Optional.of("&" + normalized);

        }

        if (normalized.matches("&#[0-9a-f]{6}")) {

            return Optional.of(normalized);

        }

        if (normalized.matches("&[0-9a-f]")) {

            return Optional.of(normalized);

        }

        if (normalized.matches("[0-9a-f]")) {

            return Optional.of("&" + normalized);

        }

        return Optional.empty();

    }

    @Subcommand("%invite")
    @CommandPermission("unionsog.member.invite")
    @CommandCompletion("@non_members:ignore_vanished")
    @Conditions("union_member")
    @Description("{@@command.description.invite}")
    public void invite(Player sender, UnionPlayer cp, Union union,
            @Conditions("not_banned|not_in_union|online:ignore_vanished") @Name("player") UnionPlayerInput invited)
    {

        if (!invited.getUnionPlayer().isInviteEnabled()) {

            ChatBlock.sendMessage(sender, RED + lang("invitedplayer.invite.off", sender));
            return;

        }

        Player invitedPlayer = invited.getUnionPlayer().toPlayer();
        if (invitedPlayer == null)
            return;
        if (!permissions.has(invitedPlayer, "unionsog.member.can-join")) {

            ChatBlock.sendMessage(sender,
                    RED + lang("the.player.doesn.t.not.have.the.permissions.to.join.unions", sender));
            return;

        }

        if (invitedPlayer.getUniqueId().equals(sender.getUniqueId())) {

            ChatBlock.sendMessage(sender, RED + lang("you.cannot.invite.yourself", sender));
            return;

        }

        long minutesBeforeRejoin = cm.getMinutesBeforeRejoin(invited.getUnionPlayer(), union);
        if (minutesBeforeRejoin != 0) {

            ChatBlock.sendMessage(sender,
                    RED + lang("the.player.must.wait.0.before.joining.your.union.again", sender, minutesBeforeRejoin));
            return;

        }

        if (union.getSize() >= settings.getInt(UNION_MAX_MEMBERS) && settings.getInt(UNION_MAX_MEMBERS) > 0) {

            ChatBlock.sendMessage(sender, RED + lang("the.union.members.reached.limit", sender));
            return;

        }

        if (!cm.purchaseInvite(sender)) {

            return;

        }

        requestManager.addInviteRequest(cp, invitedPlayer.getName(), union);
        ChatBlock.sendMessage(sender,
                AQUA + lang("has.been.asked.to.join", sender, invitedPlayer.getName(), union.getName()));

    }

    @Subcommand("%unionff %allow")
    @CommandPermission("unionsog.member.union-ff")
    @Conditions("union_member")
    @Description("{@@command.description.unionff.allow}")
    public void allowUnionFf(Player player, Union union) {

        union.addBb(player.getName(), lang("union.wide.friendly.fire.is.allowed"));
        union.setFriendlyFire(true);
        storage.updateUnion(union);

    }

    @Subcommand("%unionff %block")
    @CommandPermission("unionsog.member.union-ff")
    @Conditions("union_member")
    @Description("{@@command.description.unionff.block}")
    public void blockUnionFf(Player player, Union union) {

        union.addBb(player.getName(), lang("union.wide.friendly.fire.blocked"));
        union.setFriendlyFire(false);
        storage.updateUnion(union);

    }

    @Subcommand("%description")
    @CommandPermission("unionsog.member.description")
    @Conditions("union_member")
    @Description("{@@command.description.description}")
    public void setDescription(Player player, Union union, @Name("description") String description) {

        if (description.length() < settings.getInt(UNION_MIN_DESCRIPTION_LENGTH)) {

            ChatBlock.sendMessage(player, RED + lang("your.union.description.must.be.longer.than", player,
                    settings.getInt(UNION_MIN_DESCRIPTION_LENGTH)));
            return;

        }

        if (description.length() > settings.getInt(UNION_MAX_DESCRIPTION_LENGTH)) {

            ChatBlock.sendMessage(player, RED + lang("your.union.description.cannot.be.longer.than", player,
                    settings.getInt(UNION_MAX_DESCRIPTION_LENGTH)));
            return;

        }

        union.setDescription(description);
        ChatBlock.sendMessage(player, AQUA + lang("description.changed", player));
        storage.updateUnion(union);

    }

    @Subcommand("%rename")
    @CommandPermission("unionsog.member.rename")
    @Conditions("union_member")
    @Description("{@@command.description.rename}")
    public void rename(Player player, Union union, @Name("name") String unionName) {

        if (unionName.contains("&")) {

            ChatBlock.sendMessageKey(player, "your.union.name.cannot.contain.color.codes");
            return;

        }

        if (ChatUtils.stripColors(unionName).trim().equalsIgnoreCase("None")) {

            ChatBlock.sendMessage(player, RED + "Your union cannot be named \"None\".");
            return;

        }

        boolean bypass = permissions.has(player, "unionsog.mod.bypass");
        if (!bypass) {

            if (ChatUtils.stripColors(unionName).length() > settings.getInt(UNION_MAX_LENGTH)) {

                ChatBlock.sendMessage(player, RED + lang("your.union.name.cannot.be.longer.than.characters", player,
                        settings.getInt(UNION_MAX_LENGTH)));
                return;

            }

            if (ChatUtils.stripColors(unionName).length() <= settings.getInt(UNION_MIN_LENGTH)) {

                ChatBlock.sendMessage(player, RED + lang("your.union.name.must.be.longer.than.characters", player,
                        settings.getInt(UNION_MIN_LENGTH)));
                return;

            }

        }

        union.addBb(player.getName(), lang("union.renamed.to.0", unionName));
        union.setName(unionName);
        storage.updateUnion(union);

        ChatBlock.sendMessageKey(player, "you.have.successfully.renamed.your.union", unionName);

    }

    @Subcommand("%rival %add")
    @CommandPermission("unionsog.member.rival")
    @Conditions("rivable|minimum_to_rival")
    @CommandCompletion("@unions:hide_own")
    @Description("{@@command.description.rival.add}")
    public void addRival(Player player, Union issuerUnion, @Conditions("different") @Name("union") UnionInput rival) {

        Union rivalInput = rival.getUnion();
        if (settings.isUnrivable(rivalInput.getTag())) {

            ChatBlock.sendMessage(player, RED + lang("the.union.cannot.be.rivaled", player));
            return;

        }

        if (!issuerUnion.reachedRivalLimit()) {

            if (!issuerUnion.isRival(rivalInput.getTag())) {

                issuerUnion.addRival(rivalInput);
                rivalInput.addBb(player.getName(),
                        lang("has.initiated.a.rivalry", issuerUnion.getName(), rivalInput.getName()), false);
                issuerUnion.addBb(player.getName(),
                        lang("has.initiated.a.rivalry", player.getName(), rivalInput.getName()));

            } else {

                ChatBlock.sendMessage(player, RED + lang("your.unions.are.already.rivals", player));

            }

        } else {

            ChatBlock.sendMessage(player, RED + lang("rival.limit.reached", player));

        }

    }

    @Subcommand("%rival %remove")
    @CommandPermission("unionsog.member.rival")
    @Conditions("union_member")
    @CommandCompletion("@rivals")
    @Description("{@@command.description.rival.remove}")
    public void removeRival(Player player, UnionPlayer cp, Union issuerUnion,
            @Conditions("different") @Name("union") UnionInput rival)
    {

        Union rivalInput = rival.getUnion();
        if (issuerUnion.isRival(rivalInput.getTag())) {

            requestManager.addRivalryBreakRequest(cp, rivalInput, issuerUnion);
            ChatBlock.sendMessage(player, AQUA + lang("leaders.asked.to.end.rivalry", player, rivalInput.getName()));

        } else {

            ChatBlock.sendMessage(player, RED + lang("your.unions.are.not.rivals", player));

        }

    }

    @Subcommand("%ally %add")
    @CommandPermission("unionsog.member.ally-set")
    @Conditions("minimum_to_ally")
    @CommandCompletion("@unions:hide_own")
    @Description("{@@command.description.ally.add}")
    public void addAlly(Player player, UnionPlayer cp, Union issuerUnion,
            @Conditions("different") @Name("union") UnionInput other)
    {

        Union input = other.getUnion();
        if (issuerUnion.isAlly(input.getTag())) {

            ChatBlock.sendMessage(player, RED + lang("your.unions.are.already.allies", player));
            return;

        }

        int maxAlliances = settings.getInt(UNION_MAX_ALLIANCES);
        if (maxAlliances != -1) {

            if (issuerUnion.getAllies().size() >= maxAlliances) {

                ChatBlock.sendMessage(player, lang("your.union.reached.max.alliances", player));
                return;

            }

            if (input.getAllies().size() >= maxAlliances) {

                ChatBlock.sendMessage(player, lang("other.union.reached.max.alliances", player));
                return;

            }

        }

        requestManager.addAllyRequest(cp, input, issuerUnion);
        ChatBlock.sendMessage(player, AQUA + lang("leaders.have.been.asked.for.an.alliance", player, input.getName()));

    }

    @Subcommand("%ally %remove")
    @Conditions("union_member")
    @CommandPermission("unionsog.member.ally-set")
    @Description("{@@command.description.ally.remove}")
    @CommandCompletion("@allied_unions")
    public void removeAlly(Player player, Union issuerUnion,
            @Conditions("different|allied_union") @Name("union") UnionInput ally)
    {

        Union allyInput = ally.getUnion();
        issuerUnion.removeAlly(allyInput);
        allyInput.addBb(player.getName(), lang("has.broken.the.alliance", issuerUnion.getName(), allyInput.getName()),
                false);
        issuerUnion.addBb(player.getName(), lang("has.broken.the.alliance", player.getName(), allyInput.getName()));

    }

    @Subcommand("%kick")
    @CommandPermission("unionsog.member.kick")
    @CommandCompletion("@union_members:hide_own")
    @Conditions("union_member")
    @Description("{@@command.description.kick}")
    public void kick(@Conditions("union_member") Player sender,
            @Conditions("same_union") @Name("member") UnionPlayerInput other)
    {

        UnionPlayer unionPlayer = other.getUnionPlayer();
        if (sender.getUniqueId().equals(unionPlayer.getUniqueId())) {

            ChatBlock.sendMessage(sender, RED + lang("you.cannot.kick.yourself", sender));
            return;

        }

        Union union = Objects.requireNonNull(cm.getUnionByPlayerUniqueId(sender.getUniqueId()));

        union.addBb(sender.getName(), lang("has.been.kicked.by", unionPlayer.getName(), sender.getName(), sender));
        union.removePlayerFromUnion(unionPlayer.getUniqueId());

    }

    @Subcommand("%resign %confirm")
    @CommandPermission("unionsog.member.resign")
    @Description("{@@command.description.resign}")
    @HelpSearchTags("leave")
    public void resignConfirm(Player player, UnionPlayer cp, Union union) {

        if (union.isPermanent() || union.getSize() > 1) {

            union.addBb(player.getName(), lang("0.has.resigned", player.getName()));
            cp.addResignTime(union.getTag());
            union.removePlayerFromUnion(player.getUniqueId());

            ChatBlock.sendMessage(cp, AQUA + lang("resign.success", player));

        } else {

            union.disband(player, true, false);
            ChatBlock.sendMessage(cp, RED + lang("union.has.been.disbanded", player, union.getName()));

        }

    }

    @Subcommand("%resign")
    @CommandPermission("unionsog.member.resign")
    @Description("{@@command.description.resign}")
    @HelpSearchTags("leave")
    public void resign(@Conditions("union_member") Player player) {

        new SCConversation(plugin, player, new ResignPrompt()).begin();

    }

}
