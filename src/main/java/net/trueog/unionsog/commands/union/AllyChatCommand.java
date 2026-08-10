package net.trueog.unionsog.commands.union;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import net.trueog.unionsog.ChatBlock;
import net.trueog.unionsog.UnionPlayer;
import net.trueog.unionsog.managers.ChatManager;
import net.trueog.unionsog.managers.SettingsManager;
import net.trueog.unionsog.managers.StorageManager;

import static net.trueog.unionsog.UnionPlayer.Channel.ALLY;
import static net.trueog.unionsog.UnionPlayer.Channel.NONE;
import static net.trueog.unionsog.UnionsOG.lang;
import static net.trueog.unionsog.chat.SCMessage.Source.SPIGOT;

@CommandAlias("%ally_chat")
@Description("{@@command.description.ally}")
@CommandPermission("unionsog.member.ally")
@Conditions("%basic_conditions|union_member|can_chat:type=ALLY")
public class AllyChatCommand extends BaseCommand {

    @Dependency
    private ChatManager chatManager;
    @Dependency
    private SettingsManager settingsManager;
    @Dependency
    private StorageManager storageManager;

    @Default
    @HelpSearchTags("chat")
    public void sendMessage(UnionPlayer cp, @Name("message") String message) {

        chatManager.processChat(SPIGOT, ALLY, cp, message);

    }

    @Subcommand("%join")
    public void join(UnionPlayer unionPlayer) {

        if (unionPlayer.getChannel() == ALLY) {

            ChatBlock.sendMessage(unionPlayer, lang("already.joined.ally.chat"));
            return;

        }

        unionPlayer.setChannel(ALLY);
        storageManager.updateUnionPlayer(unionPlayer);
        ChatBlock.sendMessage(unionPlayer, lang("joined.ally.chat"));

    }

    @Subcommand("%leave")
    public void leave(UnionPlayer unionPlayer) {

        if (unionPlayer.getChannel() == ALLY) {

            unionPlayer.setChannel(NONE);
            storageManager.updateUnionPlayer(unionPlayer);
            ChatBlock.sendMessage(unionPlayer, lang("left.ally.chat", unionPlayer));

        } else {

            ChatBlock.sendMessage(unionPlayer, lang("chat.didnt.join", unionPlayer));

        }

    }

    @Subcommand("%mute")
    public void mute(UnionPlayer unionPlayer) {

        if (!unionPlayer.isMutedAlly()) {

            unionPlayer.mute(ALLY, true);
            ChatBlock.sendMessage(unionPlayer, lang("muted.ally.chat", unionPlayer));

        } else {

            unionPlayer.mute(ALLY, false);
            ChatBlock.sendMessage(unionPlayer, lang("unmuted.ally.chat", unionPlayer));

        }

    }

}
