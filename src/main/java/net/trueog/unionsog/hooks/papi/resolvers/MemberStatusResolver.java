package net.trueog.unionsog.hooks.papi.resolvers;

import net.trueog.unionsog.ClanPlayer;
import net.trueog.unionsog.UnionsOG;
import net.trueog.unionsog.hooks.papi.PlaceholderBooleanFormatter;
import net.trueog.unionsog.hooks.papi.PlaceholderResolver;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.Map;

@SuppressWarnings("unused")
public class MemberStatusResolver extends PlaceholderResolver {

    public MemberStatusResolver(@NotNull UnionsOG plugin) {

        super(plugin);

    }

    @Override
    public @NotNull String getId() {

        return "member_status";

    }

    @Override
    public @NotNull String resolve(@Nullable OfflinePlayer player, @NotNull Object object, @NotNull Method method,
            @NotNull String placeholder, @NotNull Map<String, String> config)
    {

        boolean result = false;
        if (object instanceof ClanPlayer) {

            ClanPlayer cp = (ClanPlayer) object;
            if (placeholder.equals("is_member")) {

                result = cp.getClan() != null && !cp.isTrusted();

            }

            if (placeholder.equals("is_trusted")) {

                result = cp.getClan() != null && !cp.isLeader() && cp.isTrusted();

            }

        }

        return result ? PlaceholderBooleanFormatter.trueValue() : PlaceholderBooleanFormatter.falseValue();

    }

}
