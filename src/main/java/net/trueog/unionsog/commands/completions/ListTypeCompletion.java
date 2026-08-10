package net.trueog.unionsog.commands.completions;

import net.trueog.unionsog.UnionsOG;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;

import static net.trueog.unionsog.UnionsOG.lang;

@SuppressWarnings("unused")
public class ListTypeCompletion extends AbstractStaticCompletion {

    public ListTypeCompletion(@NotNull UnionsOG plugin) {

        super(plugin);

    }

    @Override
    public @NotNull Collection<String> getCompletions() {

        return Arrays.asList(lang("list.type.size"), lang("list.type.kdr"), lang("list.type.name"),
                lang("list.type.founded"), lang("list.type.active"));

    }

    @Override
    public @NotNull String getId() {

        return "union_list_type";

    }

}
