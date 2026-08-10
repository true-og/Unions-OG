package net.trueog.unionsog.hooks.papi;

import net.trueog.unionsog.Union;
import net.trueog.unionsog.UnionPlayer;
import net.trueog.unionsog.Helper;
import net.trueog.unionsog.UnionsOG;
import net.trueog.unionsog.managers.UnionManager;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UnionsOGPlaceholderEngine {

    private static final Pattern TOP_UNIONS_PATTERN = Pattern.compile("(?<strip>^topclans_(?<position>\\d+)_)clan_");
    private static final Pattern TOP_PLAYERS_PATTERN = Pattern.compile("(?<strip>^topplayers_(?<position>\\d+)_)");
    private static final String UNION_COLOR_TAG_PLACEHOLDER = "clan_color_tag";
    private static final String NO_UNION_COLOR_TAG = "&8None";

    private final UnionsOG plugin;
    private final UnionManager unionManager;
    private final Map<String, PlaceholderResolver> resolvers = new HashMap<>();

    public UnionsOGPlaceholderEngine(@NotNull UnionsOG plugin) {

        this.plugin = plugin;
        unionManager = plugin.getUnionManager();
        registerResolvers();

    }

    @NotNull
    public List<String> getLegacyPlaceholders(@NotNull String identifier) {

        LinkedHashSet<String> allPlaceholders = new LinkedHashSet<>();
        addLegacyPlaceholders(identifier + "_", getPlayerPlaceholders(), allPlaceholders);
        addLegacyPlaceholders(identifier + "_", getPlayerAliases(), allPlaceholders);
        addLegacyPlaceholders(identifier + "_clan_", getUnionPlaceholders(), allPlaceholders);
        addLegacyPlaceholders(identifier + "_union_", getUnionPlaceholdersAndAliases(), allPlaceholders);
        return new ArrayList<>(allPlaceholders);

    }

    @NotNull
    public List<String> getPlayerPlaceholders() {

        return getPlaceholderNames(UnionPlayer.class);

    }

    @NotNull
    public List<String> getPlayerAliases() {

        return getAliases(UnionPlayer.class);

    }

    @NotNull
    public List<String> getPlayerPlaceholdersAndAliases() {

        return combine(getPlayerPlaceholders(), getPlayerAliases());

    }

    @NotNull
    public List<String> getUnionPlaceholders() {

        return getPlaceholderNames(Union.class);

    }

    @NotNull
    public List<String> getUnionPlaceholdersAndAliases() {

        return combine(getUnionPlaceholders(), getAliases(Union.class));

    }

    @Nullable
    public String resolveRelational(@Nullable Player player1, @Nullable Player player2, @NotNull String params,
            @NotNull String sameUnionColor, @NotNull String rivalColor, @NotNull String allyColor)
    {

        if (player1 == null || player2 == null) {

            return null;

        }

        if (params.equalsIgnoreCase("color")) {

            UnionPlayer cp1 = unionManager.getUnionPlayer(player1);
            if (cp1 == null) {

                return "";

            }

            // noinspection ConstantConditions -- getUnionPlayer != null == getUnion() !=
            // null
            if (cp1.getUnion().isMember(player2)) {

                return sameUnionColor;

            }

            if (cp1.isRival(player2)) {

                return rivalColor;

            }

            if (cp1.isAlly(player2)) {

                return allyColor;

            }

            return "";

        }

        return null;

    }

    @NotNull
    public String resolve(@Nullable OfflinePlayer player, @NotNull String params) {

        params = normalizePlaceholder(params);

        UnionPlayer cp = null;
        if (player != null) {

            cp = unionManager.getAnyUnionPlayer(player.getUniqueId());

        }

        Union union = cp != null ? cp.getUnion() : null;

        Matcher matcher = TOP_UNIONS_PATTERN.matcher(params);
        if (matcher.find()) {

            int position = Integer.parseInt(matcher.group("position"));
            union = getFromPosition(unionManager.getUnions(), position, unionManager::sortUnionsByKDR);
            params = params.replace(matcher.group("strip"), "");
            return getValue(player, null, union, params);

        }

        matcher = TOP_PLAYERS_PATTERN.matcher(params);
        if (matcher.find()) {

            int position = Integer.parseInt(matcher.group("position"));
            cp = getFromPosition(unionManager.getAllUnionPlayers(), position, unionManager::sortUnionPlayersByKDR);
            union = cp != null ? cp.getUnion() : null;
            params = params.replace(matcher.group("strip"), "");
            return getValue(player, cp, union, params);

        }

        if (cp == null) {

            return getNoUnionValue(params);

        }

        if (union == null && UNION_COLOR_TAG_PLACEHOLDER.equals(params)) {

            return NO_UNION_COLOR_TAG;

        }

        return getValue(player, cp, union, params);

    }

    @NotNull
    private String getNoUnionValue(@NotNull String placeholder) {

        return UNION_COLOR_TAG_PLACEHOLDER.equals(placeholder) ? NO_UNION_COLOR_TAG : "";

    }

    @Nullable
    private <T> T getFromPosition(List<T> list, int position, Consumer<List<T>> sort) {

        if (isPositionValid(list, position)) {

            sort.accept(list);
            return list.get(position - 1);

        }

        return null;

    }

    private boolean isPositionValid(@NotNull Collection<?> collection, int position) {

        return position >= 1 && position <= collection.size();

    }

    @NotNull
    private String getValue(@Nullable OfflinePlayer player, @Nullable UnionPlayer cp, @Nullable Union union,
            @NotNull String placeholder)
    {

        if (placeholder.startsWith("clan_")) {

            return getValue(player, union, placeholder.substring("clan_".length()));

        }

        return getValue(player, cp, placeholder);

    }

    @NotNull
    private String getValue(@Nullable OfflinePlayer player, @Nullable Object object, @NotNull String placeholder) {

        if (object != null) {

            for (Method declaredMethod : object.getClass().getDeclaredMethods()) {

                Placeholder[] annotations = declaredMethod.getAnnotationsByType(Placeholder.class);
                for (Placeholder p : annotations) {

                    if (p.value().equals(placeholder)) {

                        return resolve(player, object, declaredMethod, p.resolver(), placeholder, p.config());

                    }

                }

            }

            plugin.getLogger().warning(String.format("Placeholder %s not found", placeholder));

        }

        return "";

    }

    private String resolve(@Nullable OfflinePlayer player, @NotNull Object object, @NotNull Method method,
            @NotNull String resolverId, @NotNull String placeholder, @NotNull String config)
    {

        PlaceholderResolver resolver = resolvers.get(resolverId);
        if (resolver != null) {

            return resolver.resolve(player, object, method, placeholder, getConfigMap(config));

        }

        plugin.getLogger().warning(String.format("Resolver %s for %s not found", resolverId, placeholder));
        return "";

    }

    @NotNull
    private Map<String, String> getConfigMap(@NotNull String config) {

        if (config.isEmpty()) {

            return Collections.emptyMap();

        }

        HashMap<String, String> map = new HashMap<>();
        String[] elements = config.split(",");
        for (String element : elements) {

            String[] keyAndValue = element.split(":");
            map.put(keyAndValue[0], keyAndValue.length > 1 ? keyAndValue[1] : null);

        }

        return map;

    }

    private void registerResolvers() {

        Set<Class<? extends PlaceholderResolver>> placeholderResolvers = Helper
                .getSubTypesOf("net.trueog.unionsog.hooks.papi.resolvers", PlaceholderResolver.class);
        plugin.getLogger().info(String.format("Registering %d placeholder resolvers...", placeholderResolvers.size()));
        for (Class<? extends PlaceholderResolver> resolverClass : placeholderResolvers) {

            try {

                PlaceholderResolver resolver = resolverClass.getConstructor(UnionsOG.class).newInstance(plugin);
                resolvers.put(resolver.getId(), resolver);

            } catch (InstantiationException | IllegalAccessException | InvocationTargetException
                    | NoSuchMethodException e)
            {

                plugin.getLogger().log(Level.SEVERE, "Error registering placeholder resolver", e);

            }

        }

    }

    private void addLegacyPlaceholders(String prefix, Collection<String> placeholderNames,
            Collection<String> placeholders)
    {

        for (String placeholderName : placeholderNames) {

            placeholders.add("%" + prefix + placeholderName + "%");

        }

    }

    @NotNull
    private List<String> getPlaceholderNames(Class<?> clazz) {

        LinkedHashSet<String> placeholders = new LinkedHashSet<>();
        for (Method method : clazz.getDeclaredMethods()) {

            Placeholder[] annotations = method.getAnnotationsByType(Placeholder.class);
            for (Placeholder annotation : annotations) {

                placeholders.add(annotation.value());

            }

        }

        return new ArrayList<>(placeholders);

    }

    @NotNull
    private List<String> getAliases(Class<?> clazz) {

        LinkedHashSet<String> aliases = new LinkedHashSet<>();
        for (String placeholder : getPlaceholderNames(clazz)) {

            String alias = toUnionAlias(placeholder);
            if (!alias.equals(placeholder)) {

                aliases.add(alias);

            }

        }

        return new ArrayList<>(aliases);

    }

    @NotNull
    private List<String> combine(Collection<String> first, Collection<String> second) {

        LinkedHashSet<String> combined = new LinkedHashSet<>(first);
        combined.addAll(second);
        return new ArrayList<>(combined);

    }

    @NotNull
    private String normalizePlaceholder(@NotNull String placeholder) {

        String normalized = placeholder;
        if (normalized.startsWith("topunions_")) {

            normalized = "topclans_" + normalized.substring("topunions_".length());

        }

        if (normalized.startsWith("union_")) {

            normalized = "clan_" + normalized.substring("union_".length());

        }

        normalized = normalized.replace("_union_", "_clan_");
        normalized = normalized.replace("in_union", "in_clan");
        normalized = normalized.replace("unionchat_player_color", "clanchat_player_color");
        normalized = normalized.replace("topunions_position", "topclans_position");

        return normalized;

    }

    @NotNull
    private String toUnionAlias(@NotNull String placeholder) {

        return placeholder.replace("topclans", "topunions").replace("in_clan", "in_union")
                .replace("clanchat_player_color", "unionchat_player_color");

    }

}
