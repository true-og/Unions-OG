package net.trueog.unionsog.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChatUtilsApplyCaseTest {

    @Test
    void recasesPlainTag() {

        assertEquals("OGS", ChatUtils.applyCase("OGs", "OGS"));

    }

    @Test
    void keepsLeadingColorCode() {

        assertEquals("§bOGS", ChatUtils.applyCase("§bOGs", "OGS"));

    }

    @Test
    void keepsEmbeddedColorCodes() {

        assertEquals("§4O§6GS", ChatUtils.applyCase("§4O§6Gs", "OGS"));

    }

    @Test
    void keepsHexColorCode() {

        assertEquals("§x§f§f§0§0§0§0OGS", ChatUtils.applyCase("§x§f§f§0§0§0§0OGs", "OGS"));

    }

    @Test
    void keepsLeftoverCharactersWhenLengthsDiffer() {

        assertEquals("OGs", ChatUtils.applyCase("OGs", "OG"));

    }

}
