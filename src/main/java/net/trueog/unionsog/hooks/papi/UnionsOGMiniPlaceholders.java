package net.trueog.unionsog.hooks.papi;

import net.trueog.unionsog.UnionsOG;
import net.trueog.utilitiesog.UtilitiesOG;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class UnionsOGMiniPlaceholders {

    private static final List<String> IDENTIFIERS = Arrays.asList("simpleclans", "simpleunions");
    private static final String SAME_UNION_COLOR = "&a";
    private static final String RIVAL_COLOR = "&c";
    private static final String ALLY_COLOR = "&b";

    private final UnionsOGPlaceholderEngine placeholders;

    public UnionsOGMiniPlaceholders(@NotNull UnionsOG plugin) {

        placeholders = new UnionsOGPlaceholderEngine(plugin);

    }

    public void register() {

        for (String identifier : IDENTIFIERS) {

            registerRelationalColor(identifier);
            registerPlayerPlaceholders(identifier);
            registerUnionPlaceholders(identifier);
            registerTopPlayerPlaceholders(identifier);
            registerTopUnionPlaceholders(identifier);

        }

    }

    private void registerRelationalColor(@NotNull String identifier) {

        UtilitiesOG.registerRelationalPlaceholder(identifier + "_color", (player, target) -> nullToEmpty(
                placeholders.resolveRelational(player, target, "color", SAME_UNION_COLOR, RIVAL_COLOR, ALLY_COLOR)));

    }

    private void registerPlayerPlaceholders(@NotNull String identifier) {

        for (String placeholder : placeholders.getPlayerPlaceholdersAndAliases()) {

            registerAudiencePlaceholder(identifier + "_" + placeholder, placeholder);

        }

    }

    private void registerUnionPlaceholders(@NotNull String identifier) {

        for (String placeholder : placeholders.getUnionPlaceholders()) {

            registerAudiencePlaceholder(identifier + "_clan_" + placeholder, "clan_" + placeholder);

        }

        for (String placeholder : placeholders.getUnionPlaceholdersAndAliases()) {

            registerAudiencePlaceholder(identifier + "_union_" + placeholder, "union_" + placeholder);

        }

    }

    private void registerTopPlayerPlaceholders(@NotNull String identifier) {

        for (String placeholder : placeholders.getPlayerPlaceholdersAndAliases()) {

            registerPositionedAudiencePlaceholder(identifier + "_topplayers_" + placeholder,
                    (player, position) -> placeholders.resolve(player, "topplayers_" + position + "_" + placeholder));

        }

    }

    private void registerTopUnionPlaceholders(@NotNull String identifier) {

        for (String placeholder : placeholders.getUnionPlaceholders()) {

            registerPositionedAudiencePlaceholder(identifier + "_topclans_clan_" + placeholder, (player,
                    position) -> placeholders.resolve(player, "topclans_" + position + "_clan_" + placeholder));

        }

        for (String placeholder : placeholders.getUnionPlaceholdersAndAliases()) {

            registerPositionedAudiencePlaceholder(identifier + "_topunions_union_" + placeholder, (player,
                    position) -> placeholders.resolve(player, "topunions_" + position + "_union_" + placeholder));

        }

    }

    private void registerAudiencePlaceholder(@NotNull String name, @NotNull String params) {

        UtilitiesOG.registerAudiencePlaceholder(name, (Player player) -> placeholders.resolve(player, params));

    }

    private void registerPositionedAudiencePlaceholder(@NotNull String name,
            @NotNull PositionedPlaceholder placeholder)
    {

        UtilitiesOG.registerAudiencePlaceholder(name, (player, args) -> {

            if (args.isEmpty() || !isPositiveInteger(args.get(0))) {

                return "";

            }

            return placeholder.resolve(player, args.get(0));

        });

    }

    private boolean isPositiveInteger(@NotNull String value) {

        try {

            return Integer.parseInt(value) > 0;

        } catch (NumberFormatException e) {

            return false;

        }

    }

    @NotNull
    private String nullToEmpty(String value) {

        return value != null ? value : "";

    }

    @FunctionalInterface
    private interface PositionedPlaceholder {

        @NotNull
        String resolve(@NotNull Player player, @NotNull String position);

    }

}
