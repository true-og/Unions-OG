package net.trueog.unionsog.commands.completions;

import net.trueog.unionsog.Helper;
import net.trueog.unionsog.UnionsOG;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;

@SuppressWarnings("unused")
public class RankPermissionsCompletion extends AbstractStaticCompletion {

    public RankPermissionsCompletion(@NotNull UnionsOG plugin) {

        super(plugin);

    }

    @Override
    public @NotNull Collection<String> getCompletions() {

        return Arrays.asList(Helper.fromPermissionArray());

    }

    @Override
    public @NotNull String getId() {

        return "rank_permissions";

    }

}
