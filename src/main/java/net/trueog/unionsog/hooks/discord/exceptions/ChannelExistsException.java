package net.trueog.unionsog.hooks.discord.exceptions;

public class ChannelExistsException extends DiscordHookException {

    public ChannelExistsException(String debugMessage, String messageKey) {

        super(debugMessage, messageKey);

    }

}
