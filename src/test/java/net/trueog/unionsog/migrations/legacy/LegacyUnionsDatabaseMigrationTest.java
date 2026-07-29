package net.trueog.unionsog.migrations.legacy;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LegacyUnionsDatabaseMigrationTest {

    private static final UUID FIRST_ID = UUID.fromString("11111111-2222-3333-4444-555555555555");
    private static final UUID SECOND_ID = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");

    @Test
    void normalizesTagFromSchemaValues() {

        assertEquals("legacytag", LegacyUnionsDatabaseMigration.normalizedTag("&4Legacy-Tag", "Ignored", FIRST_ID));
        assertEquals("unionname", LegacyUnionsDatabaseMigration.normalizedTag(null, "Union Name", FIRST_ID));
        assertEquals("union11111111", LegacyUnionsDatabaseMigration.normalizedTag(null, "!@#", FIRST_ID));

    }

    @Test
    void assignsDeterministicSuffixToDuplicateTag() {

        final Set<String> used = new HashSet<>();
        assertEquals("same", LegacyUnionsDatabaseMigration.uniqueTag("same", FIRST_ID, used));
        assertEquals("sameaaaaaaaa", LegacyUnionsDatabaseMigration.uniqueTag("same", SECOND_ID, used));

    }

    @Test
    void mapsNamedColorsWithoutChangingUnknownColors() {

        assertEquals("&4union", LegacyUnionsDatabaseMigration.coloredTag("dark_red", "union"));
        assertEquals("&eunion", LegacyUnionsDatabaseMigration.coloredTag("yellow", "union"));
        assertEquals("union", LegacyUnionsDatabaseMigration.coloredTag("not-a-color", "union"));
        assertEquals("union", LegacyUnionsDatabaseMigration.coloredTag(null, "union"));

    }

    @Test
    void mapsPackedHomeFromObservedDatabaseShape() {

        final JsonObject flags = JsonParser
                .parseString(LegacyUnionsDatabaseMigration.homeFlags("default,world,-12.5,64,23.25,90,10"))
                .getAsJsonObject();

        assertEquals("world", flags.get("homeWorld").getAsString());
        assertEquals(-12.5, flags.get("homeX").getAsDouble());
        assertEquals(64, flags.get("homeY").getAsDouble());
        assertEquals(23.25, flags.get("homeZ").getAsDouble());
        assertEquals(90, flags.get("homeYaw").getAsDouble());
        assertEquals(10, flags.get("homePitch").getAsDouble());

    }

    @Test
    void ignoresMalformedPackedHome() {

        assertEquals("{}", LegacyUnionsDatabaseMigration.homeFlags("not,a,valid,home"));

    }

    @Test
    void createsStableSixteenCharacterFallbackName() {

        assertEquals("1111111122223333", LegacyUnionsDatabaseMigration.fallbackPlayerName(FIRST_ID));

    }

    @Test
    void rejectsSourceAndDestinationTableCollision() {

        assertThrows(IllegalArgumentException.class,
                () -> LegacyUnionsDatabaseMigration.validateTargetTablePrefix("unions_"));
        assertThrows(IllegalArgumentException.class,
                () -> LegacyUnionsDatabaseMigration.validateTargetTablePrefix("x".repeat(60)));
        assertDoesNotThrow(() -> LegacyUnionsDatabaseMigration.validateTargetTablePrefix("sc_"));

    }

}
