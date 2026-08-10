package net.trueog.unionsog.commands.union;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import net.trueog.unionsog.ChatBlock;
import net.trueog.unionsog.UnionPlayer;
import net.trueog.unionsog.managers.ChatManager;
import net.trueog.unionsog.managers.StorageManager;

import static net.trueog.unionsog.UnionPlayer.Channel.UNION;
import static net.trueog.unionsog.UnionPlayer.Channel.NONE;
import static net.trueog.unionsog.UnionsOG.lang;
import static net.trueog.unionsog.chat.SCMessage.Source.SPIGOT;

@CommandAlias("%union_chat")
@Conditions("%basic_conditions|union_member|can_chat:type=UNION")
@CommandPermission("unionsog.member.chat")
@Description("{@@command.description.chat}")
public class ChatCommand extends BaseCommand {

    @Dependency
    private ChatManager chatManager;
    @Dependency
    private StorageManager storageManager;

    @Default
    @HelpSearchTags("chat")
    public void sendMessage(UnionPlayer cp, @Optional @Name("message") String message) {

        if (message == null || message.isBlank()) {

            toggle(cp);
            return;

        }

        chatManager.processChat(SPIGOT, UNION, cp, message);

    }

    // Bare command flips the player in or out of the union chat channel.
    private void toggle(UnionPlayer unionPlayer) {

        if (unionPlayer.getChannel() == UNION) {

            unionPlayer.setChannel(NONE);
            storageManager.updateUnionPlayer(unionPlayer);
            ChatBlock.sendMessage(unionPlayer, lang("left.union.chat", unionPlayer));

        } else {

            unionPlayer.setChannel(UNION);
            storageManager.updateUnionPlayer(unionPlayer);
            ChatBlock.sendMessage(unionPlayer, lang("joined.union.chat"));

        }

    }

    @Subcommand("%join")
    public void join(UnionPlayer unionPlayer) {

        if (unionPlayer.getChannel() == UNION) {

            ChatBlock.sendMessage(unionPlayer, lang("already.joined.union.chat"));
            return;

        }

        unionPlayer.setChannel(UNION);
        storageManager.updateUnionPlayer(unionPlayer);
        ChatBlock.sendMessage(unionPlayer, lang("joined.union.chat"));

    }

    @Subcommand("%leave")
    public void leave(UnionPlayer unionPlayer) {

        if (unionPlayer.getChannel() == UNION) {

            unionPlayer.setChannel(NONE);
            storageManager.updateUnionPlayer(unionPlayer);
            ChatBlock.sendMessage(unionPlayer, lang("left.union.chat", unionPlayer));

        } else {

            ChatBlock.sendMessage(unionPlayer, lang("chat.didnt.join", unionPlayer));

        }

    }

    @Subcommand("%mute")
    public void mute(UnionPlayer unionPlayer) {

        if (!unionPlayer.isMuted()) {

            unionPlayer.mute(UNION, true);
            ChatBlock.sendMessage(unionPlayer, lang("muted.union.chat", unionPlayer));

        } else {

            unionPlayer.mute(UNION, false);
            ChatBlock.sendMessage(unionPlayer, lang("unmuted.union.chat", unionPlayer));

        }

    }

}
