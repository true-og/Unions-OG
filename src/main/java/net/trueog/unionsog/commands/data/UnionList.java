package net.trueog.unionsog.commands.data;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.trueog.unionsog.ChatBlock;
import net.trueog.unionsog.Union;
import net.trueog.unionsog.Helper;
import net.trueog.unionsog.UnionsOG;
import net.trueog.unionsog.utils.ChatUtils;
import net.trueog.unionsog.utils.KDRFormat;
import net.trueog.unionsog.utils.RankingNumberResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.trueog.unionsog.UnionsOG.lang;
import static net.trueog.unionsog.managers.SettingsManager.ConfigField.*;
import static net.trueog.unionsog.utils.RankingNumberResolver.RankingType.ORDINAL;
import static org.bukkit.ChatColor.*;

public class UnionList extends Sendable {

    private static final int UNIONS_PER_PAGE = 8;

    private final String type;
    private final String order;
    private final int page;

    public UnionList(@NotNull UnionsOG plugin, @NotNull CommandSender sender, @Nullable String type,
            @Nullable String order, @Nullable Integer page)
    {

        super(plugin, sender);

        // Allow the page number to be typed in place of the optional type and
        // order arguments, e.g. "/union list 2" or "/union list size 2".
        if (page == null && order != null && order.matches("\\d+")) {

            page = Integer.parseInt(order);
            order = null;

        }

        if (page == null && type != null && type.matches("\\d+")) {

            page = Integer.parseInt(type);
            type = null;

        }

        this.type = type == null ? sm.getString(LIST_DEFAULT_ORDER_BY) : type;
        this.order = order == null ? defaultOrder(this.type) : order;
        this.page = page == null ? 1 : Math.max(1, page);

    }

    @Override
    public void send() {

        List<Union> unions = getListableUnions();
        if (unions.isEmpty()) {

            ChatBlock.sendMessage(sender, RED + lang("no.unions.have.been.created", sender));
            return;

        }

        RankingNumberResolver<Union, ? extends Comparable<?>> ranking = getRankingResolver(unions);
        int totalPages = (unions.size() + UNIONS_PER_PAGE - 1) / UNIONS_PER_PAGE;
        int currentPage = Math.min(page, totalPages);

        sendHeader(unions);
        int start = (currentPage - 1) * UNIONS_PER_PAGE;
        for (Union union : unions.subList(start, Math.min(start + UNIONS_PER_PAGE, unions.size()))) {

            addLine(ranking, union);

        }

        chatBlock.sendBlock(sender, chatBlock.size());
        ChatBlock.sendBlank(sender);
        sendPageControls(currentPage, totalPages);
        ChatBlock.sendBlank(sender);

    }

    private String defaultOrder(String type) {

        if (type.equalsIgnoreCase(lang("list.type.name")) || type.equalsIgnoreCase(lang("list.type.founded"))) {

            return lang("list.order.asc");

        }

        return lang("list.order.desc");

    }

    private RankingNumberResolver<Union, ? extends Comparable<?>> getRankingResolver(List<Union> unions) {

        boolean ascending = lang("list.order.asc").equalsIgnoreCase(order);
        if (type.equalsIgnoreCase(lang("list.type.active"))) {

            return new RankingNumberResolver<>(unions, Union::getLastUsed, ascending, ORDINAL);

        }

        if (type.equalsIgnoreCase(lang("list.type.founded"))) {

            return new RankingNumberResolver<>(unions, Union::getFounded, ascending, ORDINAL);

        }

        if (type.equalsIgnoreCase(lang("list.type.name"))) {

            return new RankingNumberResolver<>(unions, Union::getName, ascending, ORDINAL);

        }

        if (type.equalsIgnoreCase(lang("list.type.kdr"))) {

            return new RankingNumberResolver<>(unions, union -> KDRFormat.toBigDecimal(union.getTotalKDR()), ascending,
                    sm.getRankingType());

        }

        return new RankingNumberResolver<>(unions, Union::getSize, ascending, ORDINAL);

    }

    @NotNull
    private List<Union> getListableUnions() {

        return plugin.getUnionManager().getUnions();

    }

    private void sendHeader(List<Union> unions) {

        ChatBlock.sendBlank(sender);
        ChatBlock.saySingle(sender, sm.getColored(SERVER_NAME) + subColor + " " + lang("unions.lower", sender) + " "
                + headColor + Helper.generatePageSeparator(sm.getString(PAGE_SEPARATOR)));
        ChatBlock.sendBlank(sender);
        ChatBlock.sendMessage(sender, headColor + lang("total.unions", sender) + " " + subColor + unions.size());
        ChatBlock.sendBlank(sender);
        chatBlock.setAlignment("c", "l", "c", "c");
        chatBlock.setFlexibility(false, true, false, false);
        chatBlock.addRow("  " + headColor + lang("rank", sender), lang("name", sender), lang("kdr", sender),
                lang("members", sender));

    }

    private void addLine(RankingNumberResolver<Union, ? extends Comparable<?>> ranking, Union union) {

        String name = coloredName(union);
        String size = WHITE + "" + union.getSize();
        String kdr = YELLOW + "" + KDRFormat.format(union.getTotalKDR());

        chatBlock.addRow("  " + ranking.getRankingNumber(union), name, kdr, size);

    }

    /**
     * Renders the union once: its name, in the union's own color.
     */
    private String coloredName(Union union) {

        String colors = leadingColors(ChatUtils.parseColors(union.getColorTag()));
        if (colors.isEmpty()) {

            colors = DARK_GRAY.toString();

        }

        return colors + union.getName();

    }

    private static String leadingColors(String text) {

        StringBuilder colors = new StringBuilder();
        for (int i = 0; i + 1 < text.length() && text.charAt(i) == COLOR_CHAR; i += 2) {

            colors.append(text.charAt(i)).append(text.charAt(i + 1));

        }

        return colors.toString();

    }

    private void sendPageControls(int currentPage, int totalPages) {

        String indicator = subColor + lang("list.page", sender, currentPage, totalPages);
        if (!(sender instanceof Player)) {

            ChatBlock.sendMessage(sender, indicator);
            if (currentPage < totalPages) {

                ChatBlock.sendMessage(sender,
                        headColor + lang("view.next.page", sender, listCommand(currentPage + 1).substring(1)));

            }

            return;

        }

        TextComponent line = new TextComponent("  ");
        line.addExtra(control("◀ " + lang("list.previous.page", sender), currentPage > 1, currentPage - 1));
        for (BaseComponent component : TextComponent.fromLegacyText(ChatUtils.parseColors("  " + indicator + "  "))) {

            line.addExtra(component);

        }

        line.addExtra(control(lang("list.next.page", sender) + " ▶", currentPage < totalPages, currentPage + 1));
        ((Player) sender).spigot().sendMessage(line);

    }

    private BaseComponent control(String label, boolean enabled, int targetPage) {

        String color = enabled ? headColor : DARK_GRAY.toString();
        TextComponent component = new TextComponent(TextComponent.fromLegacyText(ChatUtils.parseColors(color + label)));
        if (enabled) {

            component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, listCommand(targetPage)));
            component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    TextComponent.fromLegacyText(lang("hover.view.page", sender, targetPage))));

        }

        return component;

    }

    private String listCommand(int targetPage) {

        return "/" + sm.getString(COMMANDS_UNION) + " list " + type + " " + order + " " + targetPage;

    }

}
