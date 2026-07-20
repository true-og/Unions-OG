package net.trueog.unionsog.hooks.discord.exceptions;

public class ChannelsLimitException extends DiscordHookException {

    public ChannelsLimitException(String debugMessage, String messageKey) {

        super(debugMessage, messageKey);

    }

}
