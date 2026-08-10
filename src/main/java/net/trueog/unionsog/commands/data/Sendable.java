package net.trueog.unionsog.commands.data;

import net.trueog.unionsog.ChatBlock;
import net.trueog.unionsog.UnionsOG;
import net.trueog.unionsog.managers.UnionManager;
import net.trueog.unionsog.managers.SettingsManager;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import static net.trueog.unionsog.UnionsOG.lang;
import static net.trueog.unionsog.managers.SettingsManager.ConfigField.*;

public abstract class Sendable {

    protected final UnionsOG plugin;
    protected final SettingsManager sm;
    protected final UnionManager cm;
    protected final CommandSender sender;
    protected final ChatBlock chatBlock = new ChatBlock();
    protected final String headColor;
    protected final String subColor;

    public Sendable(@NotNull UnionsOG plugin, @NotNull CommandSender sender) {

        this.plugin = plugin;
        sm = plugin.getSettingsManager();
        cm = plugin.getUnionManager();
        this.sender = sender;
        headColor = sm.getColored(PAGE_HEADINGS_COLOR);
        subColor = sm.getColored(PAGE_SUBTITLE_COLOR);

    }

    protected void sendBlock() {

        SettingsManager sm = plugin.getSettingsManager();
        boolean more = chatBlock.sendBlock(sender, sm.getInt(PAGE_SIZE));

        if (more) {

            plugin.getStorageManager().addChatBlock(sender, chatBlock);
            ChatBlock.sendBlank(sender);
            ChatBlock.sendMessage(sender,
                    sm.getColored(PAGE_HEADINGS_COLOR) + lang("view.next.page", sender, sm.getString(COMMANDS_MORE)));

        }

        ChatBlock.sendBlank(sender);

    }

    public abstract void send();

}
