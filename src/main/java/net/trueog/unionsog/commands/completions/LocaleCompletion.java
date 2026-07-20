package net.trueog.unionsog.commands.completions;

import net.trueog.unionsog.UnionsOG;
import net.trueog.unionsog.language.LanguageResource;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class LocaleCompletion extends AbstractStaticCompletion {

    private final List<String> availableLocales;

    public LocaleCompletion(@NotNull UnionsOG plugin) {

        super(plugin);
        availableLocales = LanguageResource.getAvailableLocales().stream()
                .map(locale -> locale.toLanguageTag().replace("-", "_")).collect(Collectors.toList());

    }

    @Override
    public @NotNull Collection<String> getCompletions() {

        return availableLocales;

    }

    @Override
    public @NotNull String getId() {

        return "locales";

    }

}
