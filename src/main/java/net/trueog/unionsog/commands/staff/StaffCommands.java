package net.trueog.unionsog.commands.staff;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import net.trueog.unionsog.*;
import net.trueog.unionsog.commands.UnionInput;
import net.trueog.unionsog.commands.UnionPlayerInput;
import net.trueog.unionsog.events.PlayerHomeSetEvent;
import net.trueog.unionsog.events.PlayerResetKdrEvent;
import net.trueog.unionsog.events.ReloadEvent;
import net.trueog.unionsog.events.TagChangeEvent;
import net.trueog.unionsog.language.LanguageResource;
import net.trueog.unionsog.managers.UnionManager;
import net.trueog.unionsog.managers.PermissionsManager;
import net.trueog.unionsog.managers.SettingsManager;
import net.trueog.unionsog.managers.StorageManager;
import net.trueog.unionsog.ui.InventoryController;
import net.trueog.unionsog.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static net.trueog.unionsog.UnionsOG.lang;
import static net.trueog.unionsog.managers.SettingsManager.ConfigField.GLOBAL_FRIENDLY_FIRE;
import static org.bukkit.ChatColor.AQUA;
import static org.bukkit.ChatColor.RED;

@CommandAlias("%union")
@Conditions("%basic_conditions")
public class StaffCommands extends BaseCommand {

    @Dependency
    private UnionsOG plugin;
    @Dependency
    private UnionManager cm;
    @Dependency
    private PermissionsManager permissions;
    @Dependency
    private SettingsManager settings;
    @Dependency
    private StorageManager storage;

    @Subcommand("%mod %place")
    @CommandPermission("unionsog.mod.place")
    @CommandCompletion("@players @unions")
    @HelpSearchTags("move put")
    @Description("{@@command.description.place}")
    public void place(CommandSender sender, @Name("player") UnionPlayerInput cpInput,
            @Name("union") UnionInput unionInput)
    {

        UUID uuid = cpInput.getUnionPlayer().getUniqueId();
        UnionPlayer oldCp = cm.getUnionPlayer(uuid);
        Union newUnion = unionInput.getUnion();

        if (oldCp != null) {

            Union oldUnion = Objects.requireNonNull(oldCp.getUnion());

            if (oldUnion.equals(newUnion)) {

                ChatBlock.sendMessage(sender, lang("player.already.in.this.union", sender));
                return;

            }

            if (!oldUnion.isPermanent() && oldUnion.getSize() <= 1) {

                ChatBlock.sendMessage(sender, RED + lang("you.cannot.move.the.last.member", sender));
                return;

            } else {

                oldUnion.addBb(oldCp.getName(), lang("0.has.resigned", oldCp.getName()));
                oldUnion.removePlayerFromUnion(uuid);

            }

        }

        UnionPlayer cp = cm.getCreateUnionPlayer(uuid);

        newUnion.addBb(lang("joined.the.union", cp.getName()));
        cm.serverAnnounce(lang("has.joined", cp.getName(), newUnion.getName()));
        newUnion.addPlayerToUnion(cp);

    }

    @Subcommand("%mod %modtag")
    @CommandPermission("unionsog.mod.modtag")
    @Description("{@@command.description.modtag.other}")
    public void modtag(Player player, @Name("union") UnionInput unionInput, @Single @Name("tag") String tag) {

        Union union = unionInput.getUnion();
        TagChangeEvent event = new TagChangeEvent(player, union, tag);
        plugin.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {

            return;

        }

        tag = event.getNewTag();
        String cleanTag = Helper.cleanTag(tag);

        Optional<String> validationError = plugin.getTagValidator().validate(player, tag, true);
        if (validationError.isPresent()) {

            ChatBlock.sendMessage(player, validationError.get());
            return;

        }

        if (!cleanTag.equals(union.getTag())) {

            ChatBlock.sendMessage(player, RED + lang("you.can.only.modify.the.color.and.case.of.the.tag", player));
            return;

        }

        union.addBb(player.getName(), lang("tag.changed.to.0", ChatUtils.parseColors(tag)));
        union.changeUnionTag(tag);
        player.sendMessage(lang("0.tag.changed.to.1", player, union.getTag(), tag));

    }

    @Subcommand("%admin %reload")
    @CommandPermission("unionsog.admin.reload")
    @Description("{@@command.description.reload}")
    public void reload(CommandSender sender) {

        storage.saveModified();
        plugin.reloadConfig();
        LanguageResource.clearCache();
        settings.loadAndSave();
        storage.importFromDatabase();
        permissions.loadPermissions();

        for (Union union : cm.getUnions()) {

            permissions.updateUnionPermissions(union);

        }

        Bukkit.getPluginManager().callEvent(new ReloadEvent(sender));

        ChatBlock.sendMessage(sender, AQUA + lang("configuration.reloaded", sender));

    }

    @Subcommand("%mod %home %set")
    @CommandPermission("unionsog.mod.home")
    @CommandCompletion("@unions")
    @Description("{@@command.description.mod.home.set}")
    public void homeSet(Player player, UnionPlayer cp, @Name("union") UnionInput unionInput) {

        Location loc = player.getLocation();
        Union union = unionInput.getUnion();

        PlayerHomeSetEvent event = new PlayerHomeSetEvent(union, cp, loc);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {

            return;

        }

        union.setHomeLocation(loc);
        ChatBlock.sendMessage(player, AQUA + lang("hombase.mod.set", player, union.getName()) + " " + ChatColor.YELLOW
                + Helper.toLocationString(loc));

    }

    @Subcommand("%mod %home %tp")
    @CommandCompletion("@unions:has_home")
    @CommandPermission("unionsog.mod.hometp")
    @Description("{@@command.description.mod.home.tp}")
    public void homeTp(Player player, @Name("union") @Conditions("can_teleport") UnionInput union) {

        plugin.getTeleportManager().teleportToHome(player, union.getUnion());

    }

    @Subcommand("%mod %ban")
    @CommandPermission("unionsog.mod.ban")
    @CommandCompletion("@players")
    @Description("{@@command.description.ban}")
    public void ban(CommandSender sender, @Name("player") UnionPlayerInput player) {

        UUID uuid = player.getUnionPlayer().getUniqueId();
        if (settings.isBanned(uuid)) {

            ChatBlock.sendMessage(sender, RED + lang("this.player.is.already.banned", sender));
            return;

        }

        cm.ban(uuid);
        ChatBlock.sendMessage(sender, AQUA + lang("player.added.to.banned.list", sender));

        Player pl = sender.getServer().getPlayer(uuid);
        if (pl != null) {

            ChatBlock.sendMessage(pl, AQUA + lang("you.banned", sender));

        }

    }

    @Subcommand("%mod %unban")
    @CommandPermission("unionsog.mod.ban")
    @CommandCompletion("@players")
    @Description("{@@command.description.unban}")
    public void unban(CommandSender sender, @Name("player") UnionPlayerInput player) {

        UUID uuid = player.getUnionPlayer().getUniqueId();
        if (!settings.isBanned(uuid)) {

            ChatBlock.sendMessage(sender, RED + lang("this.player.is.not.banned", sender));
            return;

        }

        Player pl = Bukkit.getPlayer(uuid);
        if (pl != null) {

            ChatBlock.sendMessage(pl, AQUA + lang("you.have.been.unbanned.from.union.commands", sender));

        }

        settings.removeBanned(uuid);
        ChatBlock.sendMessage(sender, AQUA + lang("player.removed.from.the.banned.list", sender));

    }

    @Subcommand("%mod %globalff %allow")
    @CommandPermission("unionsog.mod.globalff")
    @Description("{@@command.description.globalff.allow}")
    public void allowGlobalFf(CommandSender sender) {

        if (settings.is(GLOBAL_FRIENDLY_FIRE)) {

            ChatBlock.sendMessage(sender, AQUA + lang("global.friendly.fire.is.already.being.allowed", sender));

        } else {

            settings.set(GLOBAL_FRIENDLY_FIRE, true);
            ChatBlock.sendMessage(sender, AQUA + lang("global.friendly.fire.is.set.to.allowed", sender));

        }

    }

    @Subcommand("%mod %globalff %auto")
    @CommandPermission("unionsog.mod.globalff")
    @Description("{@@command.description.globalff.auto}")
    public void autoGlobalFf(CommandSender sender) {

        if (!settings.is(GLOBAL_FRIENDLY_FIRE)) {

            ChatBlock.sendMessage(sender,
                    AQUA + lang("global.friendy.fire.is.already.being.managed.by.each.union", sender));

        } else {

            settings.set(GLOBAL_FRIENDLY_FIRE, false);
            ChatBlock.sendMessage(sender, AQUA + lang("global.friendy.fire.is.now.managed.by.each.union", sender));

        }

    }

    @Subcommand("%admin %purge")
    @CommandPermission("unionsog.admin.purge")
    @CommandCompletion("@players")
    @Description("{@@command.description.purge}")
    public void purge(CommandSender sender, @Name("player") UnionPlayerInput player) {

        Player onlinePlayer = player.getUnionPlayer().toPlayer();
        if (onlinePlayer != null && InventoryController.isRegistered(onlinePlayer)) {

            onlinePlayer.closeInventory();

        }

        Union union = player.getUnionPlayer().getUnion();
        if (union != null && union.getMembers().size() == 1) {

            union.disband(sender, false, false);

        }

        cm.deleteUnionPlayer(player.getUnionPlayer());
        ChatBlock.sendMessage(sender, AQUA + lang("player.purged", sender));

    }

    @Subcommand("%mod %kick")
    @Description("{@@command.description.mod.kick}")
    @CommandPermission("unionsog.mod.kick")
    @CommandCompletion("@all_members")
    public void kick(CommandSender sender, @Conditions("union_member") @Name("player") UnionPlayerInput cp) {

        UnionPlayer unionPlayer = cp.getUnionPlayer();
        Union union = Objects.requireNonNull(unionPlayer.getUnion());
        if (union.getSize() == 1) {

            ChatBlock.sendMessageKey(sender, "cannot.kick.last.member");
            return;

        }

        union.addBb(sender.getName(), lang("has.been.kicked.by", unionPlayer.getName(), sender.getName(), sender));
        union.removePlayerFromUnion(unionPlayer.getUniqueId());

    }

    @Subcommand("%mod %disband")
    @CommandCompletion("@unions")
    @CommandPermission("unionsog.mod.disband")
    @Description("{@@command.description.mod.disband}")
    public void disband(CommandSender sender, @Name("union") UnionInput union) {

        union.getUnion().disband(sender, true, true);

    }

    @Subcommand("%admin %resetkdr %everyone")
    @CommandPermission("unionsog.admin.resetkdr")
    @Description("{@@command.description.resetkdr.everyone}")
    public void resetKdr(CommandSender sender) {

        for (UnionPlayer cp : cm.getAllUnionPlayers()) {

            PlayerResetKdrEvent event = new PlayerResetKdrEvent(cp);
            Bukkit.getServer().getPluginManager().callEvent(event);
            if (!event.isCancelled()) {

                cm.resetKdr(cp);

            }

        }

        ChatBlock.sendMessage(sender, RED + lang("you.have.reseted.kdr.of.all.players", sender));

    }

    @Subcommand("%admin %resetkdr")
    @CommandCompletion("@players")
    @CommandPermission("unionsog.admin.resetkdr")
    @Description("{@@command.description.resetkdr.player}")
    public void resetKdr(CommandSender sender, @Name("player") UnionPlayerInput unionPlayer) {

        UnionPlayer cp = unionPlayer.getUnionPlayer();
        PlayerResetKdrEvent event = new PlayerResetKdrEvent(cp);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (!event.isCancelled()) {

            cm.resetKdr(cp);
            ChatBlock.sendMessage(sender, RED + lang("you.have.reseted.0.kdr", sender, cp.getName()));

        }

    }

    @Subcommand("%admin %permanent")
    @CommandCompletion("@unions")
    @CommandPermission("unionsog.admin.permanent")
    @Description("{@@command.description.admin.permanent}")
    public void togglePermanent(CommandSender sender, @Name("union") UnionInput unionInput) {

        Union union = unionInput.getUnion();
        boolean permanent = !union.isPermanent();
        union.setPermanent(permanent);
        union.addBb(sender.getName(),
                lang((permanent) ? "permanent.status.enabled" : "permanent.status.disabled", sender.getName()));
        ChatBlock.sendMessage(sender, AQUA + lang("you.have.toggled.permanent.status", sender, union.getName()));

    }

    @Subcommand("%mod %rename")
    @CommandCompletion("@unions @nothing")
    @CommandPermission("unionsog.mod.rename")
    @Description("{@@command.description.mod.rename}")
    public void rename(CommandSender sender, @Name("union") UnionInput unionInput, @Name("name") String unionName) {

        if (ChatUtils.stripColors(unionName).trim().equalsIgnoreCase("None")) {

            ChatBlock.sendMessage(sender, RED + "Union cannot be named \"None\".");
            return;

        }

        Union union = unionInput.getUnion();
        union.setName(unionName);
        storage.updateUnion(union);

        ChatBlock.sendMessageKey(sender, "you.have.successfully.renamed.the.union", unionName);

    }

    @Subcommand("%mod %locale")
    @CommandPermission("unionsog.mod.locale")
    @Description("{@@command.description.mod.locale}")
    @CommandCompletion("@locales")
    public void locale(CommandSender sender, @Name("player") UnionPlayerInput input,
            @Values("@locales") @Name("locale") @Single String locale)
    {

        UnionPlayer cp = input.getUnionPlayer();
        cp.setLocale(Helper.forLanguageTag(locale.replace("_", "-")));
        plugin.getStorageManager().updateUnionPlayer(cp);

        ChatBlock.sendMessage(sender, lang("locale.has.been.changed"));

    }

}
