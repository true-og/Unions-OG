package net.trueog.unionsog.commands;

import net.trueog.unionsog.Union;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class UnionInput {

    private final Union union;

    public UnionInput(@NotNull Union union) {

        this.union = union;

    }

    public Union getUnion() {

        return union;

    }

    @Override
    public boolean equals(Object o) {

        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        UnionInput unionInput = (UnionInput) o;
        return union.equals(unionInput.union);

    }

    @Override
    public int hashCode() {

        return Objects.hash(union);

    }

}
