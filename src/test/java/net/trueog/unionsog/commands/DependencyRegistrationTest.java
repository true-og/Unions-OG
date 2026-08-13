package net.trueog.unionsog.commands;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ACF injects {@code @Dependency} fields before it registers a command's
 * subcommands, and it throws when a field's type was never registered. Because
 * {@code SCCommandManager} catches that, an unregistered dependency silently
 * drops every subcommand of the class instead of failing loudly, which is how
 * {@code ProposalManager} once took out all of {@code UnionCommands} and
 * {@code HomeCommands}.
 */
public class DependencyRegistrationTest {

    private static final Path COMMANDS = Paths.get("src/main/java/net/trueog/unionsog/commands");
    private static final Path MANAGER = COMMANDS.resolve("SCCommandManager.java");

    private static final Pattern DEPENDENCY = Pattern
            .compile("@Dependency\\s+(?:private\\s+|final\\s+)*([A-Z]\\w*)\\s+\\w+\\s*;");
    private static final Pattern REGISTERED = Pattern.compile("registerDependency\\((\\w+)\\.class");

    /** ACF's BukkitCommandManager registers the plugin instance on its own. */
    private static final String PLUGIN = "UnionsOG";

    @Test
    public void everyCommandDependencyIsRegistered() throws IOException {

        Set<String> registered = matches(REGISTERED, read(MANAGER));
        registered.add(PLUGIN);

        Set<String> required = new LinkedHashSet<>();
        try (Stream<Path> sources = Files.walk(COMMANDS)) {

            List<Path> files = sources.filter(p -> p.toString().endsWith(".java")).collect(Collectors.toList());
            for (Path file : files) {

                required.addAll(matches(DEPENDENCY, read(file)));

            }

        }

        assertTrue(required.size() > 1, "found no @Dependency fields, the scan is broken");

        required.removeAll(registered);
        assertTrue(required.isEmpty(), "command @Dependency types never passed to registerDependency(): " + required);

    }

    private static String read(Path path) throws IOException {

        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);

    }

    private static Set<String> matches(Pattern pattern, String source) {

        Set<String> found = new LinkedHashSet<>();
        Matcher matcher = pattern.matcher(source);
        while (matcher.find()) {

            found.add(matcher.group(1));

        }

        return found;

    }

}
