package net.trueog.unionsog.chat;

import net.trueog.unionsog.UnionPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SCMessage implements Cloneable {

    private final Source source;
    private final UnionPlayer.Channel channel;
    private final UnionPlayer sender;
    private List<UnionPlayer> receivers;
    private String content;

    /**
     * Creates a message with initial parameters
     *
     * @param source    The source of message
     * @param channel   The channel of union player
     * @param sender    The union player sender
     * @param content   The content of message
     * @param receivers The union players, who will receive the content
     */
    public SCMessage(@NotNull Source source, @NotNull UnionPlayer.Channel channel, @NotNull UnionPlayer sender,
            String content, @NotNull List<UnionPlayer> receivers)
    {

        this.source = source;
        this.channel = channel;
        this.sender = sender;
        this.content = content;
        this.receivers = receivers;

    }

    /**
     * Creates a new SCMessage without receivers
     *
     * @see SCMessage#SCMessage(Source, UnionPlayer.Channel, UnionPlayer, String,
     *      List) instantiate with initial receievers
     */
    public SCMessage(@NotNull Source source, @NotNull UnionPlayer.Channel channel, @NotNull UnionPlayer sender,
            String content)
    {

        this(source, channel, sender, content, new ArrayList<>());

    }

    public UnionPlayer.Channel getChannel() {

        return channel;

    }

    public UnionPlayer getSender() {

        return sender;

    }

    public String getContent() {

        return content;

    }

    public Source getSource() {

        return source;

    }

    public List<UnionPlayer> getReceivers() {

        return receivers;

    }

    public void setContent(@NotNull String content) {

        this.content = content;

    }

    public void setReceivers(@NotNull List<UnionPlayer> receivers) {

        this.receivers = receivers;

    }

    @Override
    public SCMessage clone() {

        try {

            return (SCMessage) super.clone();

        } catch (CloneNotSupportedException e) {

            throw new Error(); // never thrown

        }

    }

    /**
     * The place where the message came from. Used by ChatHandlers to know which
     * SCMessages they can handle.
     *
     * @see ChatHandler initiate the handler
     */
    public enum Source {
        SPIGOT, DISCORD, PROXY
    }

}
