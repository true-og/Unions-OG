package net.trueog.unionsog.hooks.discord;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.AccountLinkedEvent;
import github.scarsz.discordsrv.api.events.AccountUnlinkedEvent;
import github.scarsz.discordsrv.api.events.DiscordGuildMessageReceivedEvent;
import github.scarsz.discordsrv.dependencies.commons.lang3.StringUtils;
import github.scarsz.discordsrv.dependencies.emoji.EmojiParser;
import github.scarsz.discordsrv.dependencies.jda.api.Permission;
import github.scarsz.discordsrv.dependencies.jda.api.entities.*;
import github.scarsz.discordsrv.dependencies.jda.api.exceptions.ErrorResponseException;
import github.scarsz.discordsrv.dependencies.jda.api.requests.Response;
import github.scarsz.discordsrv.dependencies.jda.api.requests.RestAction;
import github.scarsz.discordsrv.dependencies.kyori.adventure.text.Component;
import github.scarsz.discordsrv.objects.managers.AccountLinkManager;
import github.scarsz.discordsrv.util.DiscordUtil;
import github.scarsz.discordsrv.util.MessageUtil;
import net.trueog.unionsog.Union;
import net.trueog.unionsog.UnionPlayer;
import net.trueog.unionsog.UnionsOG;
import net.trueog.unionsog.events.*;
import net.trueog.unionsog.hooks.discord.exceptions.*;
import net.trueog.unionsog.managers.ChatManager;
import net.trueog.unionsog.managers.UnionManager;
import net.trueog.unionsog.managers.SettingsManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static github.scarsz.discordsrv.dependencies.jda.api.Permission.MANAGE_CHANNEL;
import static github.scarsz.discordsrv.dependencies.jda.api.Permission.VIEW_CHANNEL;
import static net.trueog.unionsog.UnionPlayer.Channel.UNION;
import static net.trueog.unionsog.UnionsOG.lang;
import static net.trueog.unionsog.chat.SCMessage.Source.DISCORD;
import static net.trueog.unionsog.hooks.discord.DiscordHook.DiscordAction.ADD;
import static net.trueog.unionsog.hooks.discord.DiscordHook.DiscordAction.REMOVE;
import static net.trueog.unionsog.managers.SettingsManager.ConfigField.*;

/**
 * Hooks UnionsOG and Discord, using DiscordSRV.
 * <p>
 * On server' startup:
 * </p>
 * <ul>
 * <li>Creates categories and channels, respecting discord's limits.</li>
 * <li>Removes invalid channels, resets permissions and roles.</li>
 * </ul>
 * <p>
 * Manages events:
 * </p>
 * <ul>
 * <li>Union creation/deletion</li>
 * <li>UnionPlayer joining/resigning</li>
 * <li>Player linking/unlinking</li>
 * <li>UnionPlayer promoting/demoting</li>
 * </ul>
 * <p>
 * Currently, works with union chat only.
 */
public class DiscordHook implements Listener {

    private static final int MAX_CHANNELS_PER_CATEGORY = 50;
    private static final int MAX_CHANNELS_PER_GUILD = 500;
    private final UnionsOG plugin;
    private final SettingsManager settingsManager;
    private final ChatManager chatManager;
    private final UnionManager unionManager;
    private final AccountLinkManager accountManager = DiscordSRV.getPlugin().getAccountLinkManager();
    private final List<String> textCategories;
    private final List<String> unionTags;
    private final List<String> whitelist;

    public DiscordHook(UnionsOG plugin) {

        this.plugin = plugin;
        settingsManager = plugin.getSettingsManager();
        chatManager = plugin.getChatManager();
        unionManager = plugin.getUnionManager();

        textCategories = settingsManager.getStringList(DISCORDCHAT_TEXT_CATEGORY_IDS).stream()
                .filter(this::categoryExists).collect(Collectors.toList());
        whitelist = settingsManager.getStringList(DISCORDCHAT_TEXT_WHITELIST);

        unionTags = unionManager.getUnions().stream().map(Union::getTag).collect(Collectors.toList());

        setupDiscord();

    }

    @Subscribe
    public void onMessageReceived(DiscordGuildMessageReceivedEvent event) {

        Optional<TextChannel> channel = getCachedChannel(event.getChannel().getName());

        if (channel.isPresent()) {

            Message eventMessage = event.getMessage();
            User Author = event.getAuthor();
            TextChannel textChannel = channel.get();
            UUID uuid = accountManager.getUuid(Author.getId());

            if (uuid == null) {

                sendPrivateMessage(textChannel, eventMessage, lang("you.did.not.link.your.account"));
                return;

            }

            UnionPlayer unionPlayer = unionManager.getUnionPlayer(uuid);
            if (unionPlayer == null) {

                return;

            }

            Union union = unionPlayer.getUnion();
            if (union == null) {

                return;

            }

            if (!Objects.equals(textChannel.getName(), union.getTag())) {

                String channelLink = "<#" + textChannel.getId() + ">";
                sendPrivateMessage(textChannel, eventMessage,
                        lang("cannot.send.discord.message", unionPlayer, channelLink));
                return;

            }

            // DiscordSRV start
            String emojiBehavior = DiscordSRV.config().getString("DiscordChatChannelEmojiBehavior");

            boolean hideEmoji = emojiBehavior.equalsIgnoreCase("hide");
            boolean nameEmoji = emojiBehavior.equalsIgnoreCase("name");

            Component component = MessageUtil.reserializeToMinecraft(eventMessage.getContentRaw());
            String message = MessageUtil.toLegacy(component);

            if (hideEmoji && StringUtils.isBlank(EmojiParser.removeAllEmojis(message))) {

                DiscordSRV.debug("Ignoring message from " + Author.getName()
                        + " because it became completely blank after removing unicode emojis");
                return;

            }

            if (hideEmoji) {

                // remove all emojis
                message = EmojiParser.removeAllEmojis(message);

            } else if (nameEmoji) {

                // parse emojis from unicode back to :code:
                message = EmojiParser.parseToAliases(message);

            }

            // DiscordSRV end
            chatManager.processChat(DISCORD, UNION, unionPlayer, message);

        }

    }

    @EventHandler
    public void onUnionDisband(DisbandUnionEvent event) {

        deleteChannel(event.getUnion().getTag());

    }

    @EventHandler
    public void onUnionCreate(CreateUnionEvent event) {

        try {

            if (settingsManager.is(DISCORDCHAT_AUTO_CREATION)) {

                createChannel(event.getUnion().getTag());

            }

        } catch (DiscordHookException ex) {

            // Union is not following the conditions, categories are fulled or discord
            // reaches the limit, nothing to do here.
            UnionsOG.debug(ex.getMessage());

        }

    }

    @EventHandler
    public void onPlayerUnionLeave(PlayerKickedUnionEvent event) {

        UnionPlayer unionPlayer = event.getUnionPlayer();
        Union union = event.getUnion();
        Member member = getMember(unionPlayer);
        if (member == null || union == null) {

            return;

        }

        updateViewPermission(member, union, REMOVE);

    }

    @EventHandler
    public void onPlayerUnionJoin(PlayerJoinedUnionEvent event) {

        UnionPlayer unionPlayer = event.getUnionPlayer();
        Union union = event.getUnion();
        Member member = getMember(unionPlayer);
        if (member == null || union == null) {

            return;

        }

        if (!createChannelSilently(unionPlayer)) {

            return;

        }

        updateViewPermission(member, union, ADD);

    }

    @Subscribe
    public void onPlayerLinking(AccountLinkedEvent event) {

        UnionPlayer unionPlayer = unionManager.getUnionPlayer(event.getPlayer());
        Member member = getGuild().getMember(event.getUser());
        if (unionPlayer == null || member == null) {

            return;

        }

        Union union = unionPlayer.getUnion();
        if (union == null) {

            return;

        }

        if (!createChannelSilently(unionPlayer)) {

            return;

        }

        updateViewPermission(member, union, ADD);

    }

    @Subscribe
    public void onPlayerUnlinking(AccountUnlinkedEvent event) {

        UnionPlayer unionPlayer = unionManager.getUnionPlayer(event.getPlayer());
        Member member = getGuild().getMember(event.getDiscordUser());
        if (unionPlayer == null || unionPlayer.getUnion() == null || member == null) {

            return;

        }

        updateViewPermission(member, unionPlayer.getUnion(), REMOVE);

    }

    protected void setupDiscord() {

        Map<String, TextChannel> discordTagChannels = getChannels().stream()
                .collect(Collectors.toMap(TextChannel::getName, textChannel -> textChannel));
        UnionsOG.debug("DiscordTagChannels before clearing: " + String.join(",", discordTagChannels.keySet()));

        clearChannels(discordTagChannels);
        UnionsOG.debug("DiscordTagChannels after clearing: " + String.join(",", discordTagChannels.keySet()));

        resetPermissions(discordTagChannels);

        UnionsOG.debug("ClanTags before creating: " + String.join(",", unionTags));
        createChannels(discordTagChannels);
        UnionsOG.debug("ClanTags after creating: " + String.join(",", unionTags));

    }

    @NotNull
    public Guild getGuild() {

        return DiscordSRV.getPlugin().getMainGuild();

    }

    /**
     * Creates a new UnionsOG {@link Category}
     *
     * @return Category or null, if reached the limit
     */
    @Nullable
    public Category createCategory() {

        if (getGuild().getChannels().size() >= MAX_CHANNELS_PER_GUILD) {

            return null;

        }

        String categoryName = settingsManager.getString(DISCORDCHAT_TEXT_CATEGORY_FORMAT);
        Category category = null;
        try {

            category = getGuild().createCategory(categoryName)
                    .addRolePermissionOverride(getGuild().getPublicRole().getIdLong(), Collections.emptyList(),
                            Collections.singletonList(VIEW_CHANNEL))
                    .addMemberPermissionOverride(getGuild().getSelfMember().getIdLong(),
                            Arrays.asList(VIEW_CHANNEL, MANAGE_CHANNEL), Collections.emptyList())
                    .submit().get();

            textCategories.add(category.getId());
            settingsManager.set(DISCORDCHAT_TEXT_CATEGORY_IDS, textCategories);
            settingsManager.save();

        } catch (InterruptedException | ExecutionException ex) {

            plugin.getLogger().log(Level.SEVERE, "Error while trying to create {0} category: " + ex.getMessage(),
                    categoryName);

        }

        return category;

    }

    /**
     * Creates a new {@link TextChannel} in available UnionsOG' categories,
     * otherwise creates one.
     *
     * <p>
     * Sets positive {@link Permission#VIEW_CHANNEL} permission to all linked union
     * members.
     * </p>
     *
     * @param unionTag the union tag
     * @throws InvalidChannelException  no one member is linked or union is not in
     *                                  the whitelist.
     * @throws ChannelExistsException   if channel is already exist
     * @throws CategoriesLimitException if categories reached the limit.
     * @throws ChannelsLimitException   if discord reached the channels limit.
     */
    public void createChannel(@NotNull String unionTag)
            throws InvalidChannelException, CategoriesLimitException, ChannelsLimitException, ChannelExistsException
    {

        validateChannel(unionTag);
        Map<UnionPlayer, Member> discordUnionPlayers = getDiscordPlayers(unionManager.getUnion(unionTag));

        if (getChannels().size() >= settingsManager.getInt(DISCORDCHAT_TEXT_LIMIT)) {

            throw new ChannelsLimitException("Discord reached the channels limit", "discord.reached.channels.limit");

        }

        Category availableCategory = getCachedCategories().stream()
                .filter(category -> category.getTextChannels().size() < MAX_CHANNELS_PER_CATEGORY).findAny()
                .orElseGet(this::createCategory);

        if (availableCategory == null) {

            throw new CategoriesLimitException("Discord reached the categories limit",
                    "discord.reached.category.limit");

        }

        try {

            availableCategory.createTextChannel(unionTag).complete();
            UnionsOG.debug(String.format("[%s] Creating a discord text channel for %s clan",
                    Thread.currentThread().getId(), unionTag));

        } catch (ErrorResponseException ex) {

            Response response = ex.getResponse();
            plugin.getLogger().warning(String.format("Could not create a channel for clan %s, error %d - %s", unionTag,
                    response.code, response.message));
            return;

        }

        for (Map.Entry<UnionPlayer, Member> entry : discordUnionPlayers.entrySet()) {

            // The map is formed from union#getMembers (so the union exists)
            // noinspection ConstantConditions
            updateViewPermission(entry.getValue(), entry.getKey().getUnion(), ADD);

        }

    }

    /**
     * Retrieves channel in UnionsOG categories.
     *
     * @param channelName the channel name
     * @return the channel
     * @see #getCachedCategories() retreive categories.
     */
    public Optional<TextChannel> getCachedChannel(@NotNull String channelName) {

        return getCachedChannels().stream().filter(textChannel -> textChannel.getName().equals(channelName))
                .findFirst();

    }

    /**
     * Checks if a category can be obtained by id.
     *
     * @param categoryId the category id
     * @return true if the category exists
     * @see #channelExists(String)
     */
    public boolean categoryExists(String categoryId) {

        return getGuild().getCategoryById(categoryId) != null;

    }

    /**
     * Checks if a channel with the specified union tag exists
     *
     * @see #categoryExists(String)
     */
    public boolean channelExists(String unionTag) {

        return getChannel(unionTag).isPresent();

    }

    /**
     * Retrieves the channel in UnionsOG categories.
     *
     * @param channelName the channel name
     * @return the channel
     * @see #getCachedChannel(String) retreive the <b>cached</b> channel.
     */
    public Optional<TextChannel> getChannel(@NotNull String channelName) {

        return getChannels().stream().filter(textChannel -> textChannel.getName().equals(channelName)).findAny();

    }

    /**
     * Deletes channel from UnionsOG categories. If there are no channels, removes
     * category as well.
     *
     * @param channelName the channel name
     * @return true, if channel was deleted and false if not.
     */
    @SuppressWarnings("UnusedReturnValue")
    public boolean deleteChannel(@NotNull String channelName) {

        boolean deleted = false;

        if (channelExists(channelName)) {

            for (Category category : getCachedCategories()) {

                if (!category.getTextChannels().isEmpty()) {

                    for (TextChannel textChannel : category.getTextChannels()) {

                        if (textChannel.getName().equals(channelName)) {

                            textChannel.delete().complete();
                            deleted = true;
                            break;

                        }

                    }

                    if (category.getTextChannels().isEmpty()) {

                        textCategories.remove(category.getId());
                        settingsManager.set(DISCORDCHAT_TEXT_CATEGORY_IDS, textCategories);
                        settingsManager.save();
                        category.delete().complete();

                    }

                    return deleted;

                }

            }

        }

        return false;

    }

    /**
     * @return categories from config
     */
    public List<Category> getCachedCategories() {

        return textCategories.stream().filter(this::categoryExists).map(getGuild()::getCategoryById)
                .collect(Collectors.toList());

    }

    /**
     * In most cases, you will use {@link #getCachedCategories()}.
     *
     * @return categories from guild
     */
    public List<Category> getCategories() {

        return getGuild().getCategoriesByName(settingsManager.getString(DISCORDCHAT_TEXT_CATEGORY_FORMAT), false);

    }

    /**
     * In most cases, you will use {@link #getCachedChannels()}.
     *
     * @return all channels from guild
     */
    public List<TextChannel> getChannels() {

        return getCategories().stream().map(Category::getTextChannels).flatMap(Collection::stream)
                .collect(Collectors.toList());

    }

    /**
     * @return All channels in categories
     */
    public List<TextChannel> getCachedChannels() {

        return getCachedCategories().stream().map(Category::getTextChannels).flatMap(Collection::stream)
                .collect(Collectors.toList());

    }

    @Nullable
    public Member getMember(@NotNull UnionPlayer unionPlayer) {

        String discordId = accountManager.getDiscordId(unionPlayer.getUniqueId());
        return DiscordUtil.getMemberById(discordId);

    }

    private void clearChannels(Map<String, TextChannel> discordTagChannels) {

        // Removes abandoned channels
        ArrayList<String> unionsToDelete = new ArrayList<>(discordTagChannels.keySet());
        unionsToDelete.removeAll(unionTags);
        unionsToDelete.forEach(unionChannel -> {

            deleteChannel(unionChannel);
            discordTagChannels.remove(unionChannel);

        });

        // Removes invalid channels
        Iterator<String> iterator = discordTagChannels.keySet().iterator();
        while (iterator.hasNext()) {

            String unionChannel = iterator.next();
            try {

                validateChannel(unionChannel);

            } catch (InvalidChannelException ex) {

                UnionsOG.debug(ex.getMessage());
                deleteChannel(unionChannel);
                iterator.remove();

            } catch (ChannelExistsException | ChannelsLimitException ex) {

                UnionsOG.debug(ex.getMessage());

            }

        }

    }

    private void resetPermissions(Map<String, TextChannel> discordUnionChannels) {

        for (Map.Entry<String, TextChannel> channelEntry : discordUnionChannels.entrySet()) {

            TextChannel channel = channelEntry.getValue();
            Union union = unionManager.getUnion(channelEntry.getKey());
            Map<UnionPlayer, Member> discordPlayers = getDiscordPlayers(union);

            for (Member member : discordPlayers.values()) {

                PermissionOverride override = channel.getPermissionOverride(member);
                if (override != null) {

                    override.delete().queue(afterSuccess -> updateViewPermission(member, channel, ADD));

                }

            }

        }

    }

    private void createChannels(Map<String, TextChannel> discordUnionChannels) {

        if (!settingsManager.is(DISCORDCHAT_AUTO_CREATION)) {

            return;

        }

        // Removes already used discord channels from creation
        unionTags.removeAll(discordUnionChannels.keySet());

        for (String union : unionTags) {

            try {

                createChannel(union);

            } catch (CategoriesLimitException | ChannelsLimitException ex) {

                UnionsOG.debug(ex.getMessage());
                break;

            } catch (InvalidChannelException | ChannelExistsException ignored) {

                // There is already debug on #clearChannels
            }

        }

    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void sendPrivateMessage(TextChannel textChannel, Message eventMessage, String message) {

        RestAction<PrivateChannel> privateChannelAction = eventMessage.getAuthor().openPrivateChannel();
        textChannel.deleteMessageById(eventMessage.getId())
                .queue(unused -> privateChannelAction.flatMap(privateChannel -> privateChannel.sendMessage(message)));

    }

    private void validateChannel(@NotNull String unionTag)
            throws InvalidChannelException, ChannelExistsException, ChannelsLimitException
    {

        Union union = unionManager.getUnion(unionTag);
        if (union == null) {

            throw new InvalidChannelException(String.format("Clan %s is null", unionTag));

        }

        Map<UnionPlayer, Member> discordUnionPlayers = getDiscordPlayers(union);
        if (discordUnionPlayers.isEmpty()) {

            throw new InvalidChannelException(String.format("Clan %s doesn't have any linked players", unionTag),
                    "your.union.doesnt.have.any.linked.player");

        }

        if (discordUnionPlayers.size() < settingsManager.getInt(DISCORDCHAT_MINIMUM_LINKED_PLAYERS)) {

            throw new InvalidChannelException(String.format("Clan %s doesn't have minimum linked players", unionTag),
                    "your.union.doesnt.have.minimum.linked.player");

        }

        if (!whitelist.isEmpty() && !whitelist.contains(union.getTag())) {

            throw new InvalidChannelException(String.format("Clan %s is not listed on the whitelist", unionTag),
                    "your.union.is.not.on.the.whitelist");

        }

        if (channelExists(unionTag)) {

            throw new ChannelExistsException(String.format("Channel %s is already exist", unionTag),
                    "your.union.already.has.channel");

        }

    }

    @NotNull
    private Map<UnionPlayer, Member> getDiscordPlayers(@NotNull Union union) {

        Map<UnionPlayer, Member> discordUnionPlayers = new HashMap<>();
        for (UnionPlayer cp : union.getMembers()) {

            Member member = getMember(cp);
            if (member != null) {

                discordUnionPlayers.put(cp, member);

            }

        }

        return discordUnionPlayers;

    }

    private void updateViewPermission(@Nullable Member member, @NotNull GuildChannel channel,
            @NotNull DiscordAction action)
    {

        if (member == null) {

            return;

        }

        if (action == ADD) {

            channel.upsertPermissionOverride(member)
                    .setPermissions(Collections.singletonList(VIEW_CHANNEL), Collections.emptyList()).queue();
            UnionsOG.debug(String.format("Added view permission to %s (%s) discord member", member.getNickname(),
                    member.getId()));

        } else {

            channel.getManager().removePermissionOverride(member).queue();
            UnionsOG.debug(String.format("Revoked view permission from %s (%s) discord member", member.getNickname(),
                    member.getId()));

        }

    }

    private void updateViewPermission(@NotNull Member member, @NotNull Union union, DiscordAction action) {

        String tag = union.getTag();
        Optional<TextChannel> channel = getChannel(tag);
        if (channel.isPresent()) {

            TextChannel textChannel = channel.get();
            updateViewPermission(member, textChannel, action);

        }

    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean createChannelSilently(UnionPlayer unionPlayer) {

        Union union = unionPlayer.getUnion();
        if (union == null || !settingsManager.is(DISCORDCHAT_AUTO_CREATION)) {

            return false;

        }

        try {

            createChannel(union.getTag());

        } catch (DiscordHookException ex) {

            // Union is not following the conditions, categories are fulled or discord
            // reaches the limit, nothing to do here.
            UnionsOG.debug(ex.getMessage());

        }

        return true;

    }

    enum DiscordAction {
        ADD, REMOVE
    }

}
