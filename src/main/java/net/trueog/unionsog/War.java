package net.trueog.unionsog;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class War {

    private final Map<Union, Integer> unions = new HashMap<>();

    public War(@NotNull Union union1, @NotNull Union union2) {

        unions.put(union1, 0);
        unions.put(union2, 0);

    }

    public List<Union> getUnions() {

        return new ArrayList<>(unions.keySet());

    }

    public int getTotalCasualties() {

        return unions.values().stream().mapToInt(value -> value).sum();

    }

    public int getCasualties(@NotNull Union union) {

        return unions.getOrDefault(union, 0);

    }

    public void increaseCasualties(@NotNull Union union) {

        unions.computeIfPresent(union, (c, i) -> i + 1);

    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {

            return true;

        }

        if (obj instanceof War) {

            return unions.equals(((War) obj).unions);

        }

        return false;

    }

    @Override
    public int hashCode() {

        return unions.hashCode();

    }

}
