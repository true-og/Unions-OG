package net.trueog.unionsog.commands.completions;

import net.trueog.unionsog.UnionsOG;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;

import static net.trueog.unionsog.UnionsOG.lang;

@SuppressWarnings("unused")
public class OrderCompletion extends AbstractStaticCompletion {

    public OrderCompletion(@NotNull UnionsOG plugin) {

        super(plugin);

    }

    @Override
    public @NotNull Collection<String> getCompletions() {

        return Arrays.asList(lang("list.order.asc"), lang("list.order.desc"));

    }

    @Override
    public @NotNull String getId() {

        return "order";

    }

}
