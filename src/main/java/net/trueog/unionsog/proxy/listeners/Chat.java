package net.trueog.unionsog.proxy.listeners;

import com.google.common.io.ByteArrayDataInput;
import net.trueog.unionsog.chat.SCMessage;
import net.trueog.unionsog.proxy.BungeeManager;

public class Chat extends MessageListener {

    public Chat(BungeeManager bungee) {

        super(bungee);

    }

    @Override
    public void accept(ByteArrayDataInput data) {

        SCMessage message = getGson().fromJson(data.readUTF(), SCMessage.class);
        if (message.getSender().getUnion() == null) {

            return;

        }

        bungee.getPlugin().getChatManager().processChat(message);

    }

    @Override
    public boolean isBungeeSubchannel() {

        return false;

    }

}
