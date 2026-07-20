package net.trueog.unionsog.hooks.discord.exceptions;

public class CategoriesLimitException extends DiscordHookException {

    public CategoriesLimitException(String debugMessage, String messageKey) {

        super(debugMessage, messageKey);

    }

}
