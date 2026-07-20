package net.trueog.unionsog.commands.clan;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import net.trueog.unionsog.ChatBlock;
import net.trueog.unionsog.ClanPlayer;
import net.trueog.unionsog.managers.ChatManager;
import net.trueog.unionsog.managers.SettingsManager;
import net.trueog.unionsog.managers.StorageManager;

import static net.trueog.unionsog.ClanPlayer.Channel.ALLY;
import static net.trueog.unionsog.ClanPlayer.Channel.NONE;
import static net.trueog.unionsog.UnionsOG.lang;
import static net.trueog.unionsog.chat.SCMessage.Source.SPIGOT;

@CommandAlias("%ally_chat")
@Description("{@@command.description.ally}")
@CommandPermission("unionsog.member.ally")
@Conditions("%basic_conditions|clan_member|can_chat:type=ALLY|rank:name=ALLY_CHAT")
public class AllyChatCommand extends BaseCommand {

    @Dependency
    private ChatManager chatManager;
    @Dependency
    private SettingsManager settingsManager;
    @Dependency
    private StorageManager storageManager;

    @Default
    @HelpSearchTags("chat")
    public void sendMessage(ClanPlayer cp, @Name("message") String message) {

        chatManager.processChat(SPIGOT, ALLY, cp, message);

    }

    @Subcommand("%join")
    public void join(ClanPlayer clanPlayer) {

        if (clanPlayer.getChannel() == ALLY) {

            ChatBlock.sendMessage(clanPlayer, lang("already.joined.ally.chat"));
            return;

        }

        clanPlayer.setChannel(ALLY);
        storageManager.updateClanPlayer(clanPlayer);
        ChatBlock.sendMessage(clanPlayer, lang("joined.ally.chat"));

    }

    @Subcommand("%leave")
    public void leave(ClanPlayer clanPlayer) {

        if (clanPlayer.getChannel() == ALLY) {

            clanPlayer.setChannel(NONE);
            storageManager.updateClanPlayer(clanPlayer);
            ChatBlock.sendMessage(clanPlayer, lang("left.ally.chat", clanPlayer));

        } else {

            ChatBlock.sendMessage(clanPlayer, lang("chat.didnt.join", clanPlayer));

        }

    }

    @Subcommand("%mute")
    public void mute(ClanPlayer clanPlayer) {

        if (!clanPlayer.isMutedAlly()) {

            clanPlayer.mute(ALLY, true);
            ChatBlock.sendMessage(clanPlayer, lang("muted.ally.chat", clanPlayer));

        } else {

            clanPlayer.mute(ALLY, false);
            ChatBlock.sendMessage(clanPlayer, lang("unmuted.ally.chat", clanPlayer));

        }

    }

}
