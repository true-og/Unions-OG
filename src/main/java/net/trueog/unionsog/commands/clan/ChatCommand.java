package net.trueog.unionsog.commands.clan;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import net.trueog.unionsog.ChatBlock;
import net.trueog.unionsog.ClanPlayer;
import net.trueog.unionsog.managers.ChatManager;
import net.trueog.unionsog.managers.StorageManager;

import static net.trueog.unionsog.ClanPlayer.Channel.CLAN;
import static net.trueog.unionsog.ClanPlayer.Channel.NONE;
import static net.trueog.unionsog.UnionsOG.lang;
import static net.trueog.unionsog.chat.SCMessage.Source.SPIGOT;

@CommandAlias("%clan_chat")
@Conditions("%basic_conditions|clan_member|can_chat:type=CLAN")
@CommandPermission("unionsog.member.chat")
@Description("{@@command.description.chat}")
public class ChatCommand extends BaseCommand {

    @Dependency
    private ChatManager chatManager;
    @Dependency
    private StorageManager storageManager;

    @Default
    @HelpSearchTags("chat")
    public void sendMessage(ClanPlayer cp, @Optional @Name("message") String message) {

        if (message == null || message.isBlank()) {

            toggle(cp);
            return;

        }

        chatManager.processChat(SPIGOT, CLAN, cp, message);

    }

    // Bare command flips the player in or out of the union chat channel.
    private void toggle(ClanPlayer clanPlayer) {

        if (clanPlayer.getChannel() == CLAN) {

            clanPlayer.setChannel(NONE);
            storageManager.updateClanPlayer(clanPlayer);
            ChatBlock.sendMessage(clanPlayer, lang("left.clan.chat", clanPlayer));

        } else {

            clanPlayer.setChannel(CLAN);
            storageManager.updateClanPlayer(clanPlayer);
            ChatBlock.sendMessage(clanPlayer, lang("joined.clan.chat"));

        }

    }

    @Subcommand("%join")
    public void join(ClanPlayer clanPlayer) {

        if (clanPlayer.getChannel() == CLAN) {

            ChatBlock.sendMessage(clanPlayer, lang("already.joined.clan.chat"));
            return;

        }

        clanPlayer.setChannel(CLAN);
        storageManager.updateClanPlayer(clanPlayer);
        ChatBlock.sendMessage(clanPlayer, lang("joined.clan.chat"));

    }

    @Subcommand("%leave")
    public void leave(ClanPlayer clanPlayer) {

        if (clanPlayer.getChannel() == CLAN) {

            clanPlayer.setChannel(NONE);
            storageManager.updateClanPlayer(clanPlayer);
            ChatBlock.sendMessage(clanPlayer, lang("left.clan.chat", clanPlayer));

        } else {

            ChatBlock.sendMessage(clanPlayer, lang("chat.didnt.join", clanPlayer));

        }

    }

    @Subcommand("%mute")
    public void mute(ClanPlayer clanPlayer) {

        if (!clanPlayer.isMuted()) {

            clanPlayer.mute(CLAN, true);
            ChatBlock.sendMessage(clanPlayer, lang("muted.clan.chat", clanPlayer));

        } else {

            clanPlayer.mute(CLAN, false);
            ChatBlock.sendMessage(clanPlayer, lang("unmuted.clan.chat", clanPlayer));

        }

    }

}
