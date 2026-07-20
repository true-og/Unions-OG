package net.trueog.unionsog.commands.clan;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import net.trueog.unionsog.ChatBlock;
import net.trueog.unionsog.ClanPlayer;
import net.trueog.unionsog.hooks.protection.Land;
import net.trueog.unionsog.managers.ProtectionManager;
import net.trueog.unionsog.managers.SettingsManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static net.trueog.unionsog.managers.SettingsManager.ConfigField.LAND_EDIT_ALL_LANDS;

@CommandAlias("%clan")
@Subcommand("%land")
@Conditions("%basic_conditions|clan_member|land_sharing|own_land")
@CommandPermission("unionsog.member.land")
public class LandCommand extends BaseCommand {

    @Dependency
    private ProtectionManager protection;
    @Dependency
    private SettingsManager settings;

    private Set<Land> getLands(ClanPlayer cp, Location location) {

        Player player = Objects.requireNonNull(cp.toPlayer());
        if (settings.is(LAND_EDIT_ALL_LANDS)) {

            return protection.getLands(player, location);

        } else {

            return protection.getLandsAt(location).stream().filter(l -> l.getOwners().contains(player.getUniqueId()))
                    .collect(Collectors.toSet());

        }

    }

    @Subcommand("%allow")
    public class AllowCommand extends BaseCommand {

        private void allow(ClanPlayer cp, ProtectionManager.Action action, Location location) {

            allow(cp, action, location, true);

        }

        private void allow(ClanPlayer cp, ProtectionManager.Action action, Location location, boolean notify) {

            for (Land land : getLands(cp, location)) {

                cp.allow(action, land.getId());

            }

            if (notify) {

                ChatBlock.sendMessageKey(cp, "land.allowed.action");

            }

        }

        @Subcommand("%container")
        @Description("{@@command.description.land.allow.container}")
        @CommandPermission("unionsog.member.land.allow.container")
        public void container(Player player, ClanPlayer cp) {

            allow(cp, ProtectionManager.Action.CONTAINER, player.getLocation());

        }

        @Subcommand("%place")
        @Description("{@@command.description.land.allow.place}")
        @CommandPermission("unionsog.member.land.allow.place")
        public void place(Player player, ClanPlayer cp) {

            allow(cp, ProtectionManager.Action.PLACE, player.getLocation());

        }

        @Subcommand("%damage")
        @Description("{@@command.description.land.allow.damage}")
        @CommandPermission("unionsog.member.land.allow.damage")
        public void damage(Player player, ClanPlayer cp) {

            allow(cp, ProtectionManager.Action.DAMAGE, player.getLocation());

        }

        @Subcommand("%interact_entity")
        @Description("{@@command.description.land.allow.interact_entity}")
        @CommandPermission("unionsog.member.land.allow.interact_entity")
        public void interactEntity(Player player, ClanPlayer cp) {

            allow(cp, ProtectionManager.Action.INTERACT_ENTITY, player.getLocation());

        }

        @Subcommand("%interact")
        @Description("{@@command.description.land.allow.interact}")
        @CommandPermission("unionsog.member.land.allow.interact")
        public void interact(Player player, ClanPlayer cp) {

            allow(cp, ProtectionManager.Action.INTERACT, player.getLocation());

        }

        @Subcommand("%break")
        @Description("{@@command.description.land.allow.break}")
        @CommandPermission("unionsog.member.land.allow.break")
        public void allowBreak(Player player, ClanPlayer cp) {

            allow(cp, ProtectionManager.Action.BREAK, player.getLocation());

        }

        @Subcommand("%all")
        @Description("{@@command.description.land.allow.all}")
        @CommandPermission("unionsog.member.land.allow.all")
        public void all(Player player, ClanPlayer cp) {

            for (ProtectionManager.Action action : ProtectionManager.Action.values()) {

                allow(cp, action, player.getLocation(), false);

            }

            ChatBlock.sendMessageKey(player, "land.allowed.all.actions");

        }

    }

    @Subcommand("%block")
    public class BlockCommand extends BaseCommand {

        private void block(ClanPlayer cp, ProtectionManager.Action action, Location location) {

            block(cp, action, location, true);

        }

        private void block(ClanPlayer cp, ProtectionManager.Action action, Location location, boolean notify) {

            for (Land land : getLands(cp, location)) {

                cp.disallow(action, land.getId());

            }

            if (notify) {

                ChatBlock.sendMessageKey(cp, "land.blocked.action");

            }

        }

        @Subcommand("%container")
        @Description("{@@command.description.land.block.container}")
        @CommandPermission("unionsog.member.land.block.container")
        public void container(Player player, ClanPlayer cp) {

            block(cp, ProtectionManager.Action.CONTAINER, player.getLocation());

        }

        @Subcommand("%place")
        @Description("{@@command.description.land.block.place}")
        @CommandPermission("unionsog.member.land.block.place")
        public void place(Player player, ClanPlayer cp) {

            block(cp, ProtectionManager.Action.PLACE, player.getLocation());

        }

        @Subcommand("%damage")
        @Description("{@@command.description.land.block.damage}")
        @CommandPermission("unionsog.member.land.block.damage")
        public void damage(Player player, ClanPlayer cp) {

            block(cp, ProtectionManager.Action.DAMAGE, player.getLocation());

        }

        @Subcommand("%interact_entity")
        @Description("{@@command.description.land.block.interact_entity}")
        @CommandPermission("unionsog.member.land.block.interact_entity")
        public void interactEntity(Player player, ClanPlayer cp) {

            block(cp, ProtectionManager.Action.INTERACT_ENTITY, player.getLocation());

        }

        @Subcommand("%interact")
        @Description("{@@command.description.land.block.interact}")
        @CommandPermission("unionsog.member.land.block.interact")
        public void interact(Player player, ClanPlayer cp) {

            block(cp, ProtectionManager.Action.INTERACT, player.getLocation());

        }

        @Subcommand("%break")
        @Description("{@@command.description.land.block.break}")
        @CommandPermission("unionsog.member.land.block.break")
        public void blockBreak(Player player, ClanPlayer cp) {

            block(cp, ProtectionManager.Action.BREAK, player.getLocation());

        }

        @Subcommand("%all")
        @Description("{@@command.description.land.block.all}")
        @CommandPermission("unionsog.member.land.block.all")
        public void all(Player player, ClanPlayer cp) {

            protection.getLandsAt(player.getLocation());
            for (ProtectionManager.Action action : ProtectionManager.Action.values()) {

                block(cp, action, player.getLocation(), false);

            }

            ChatBlock.sendMessageKey(player, "land.blocked.all.actions");

        }

    }

}
