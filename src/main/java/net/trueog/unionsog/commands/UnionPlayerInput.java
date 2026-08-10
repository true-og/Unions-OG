package net.trueog.unionsog.commands;

import net.trueog.unionsog.UnionPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class UnionPlayerInput {

    private final UnionPlayer unionPlayer;

    public UnionPlayerInput(@NotNull UnionPlayer unionPlayer) {

        this.unionPlayer = unionPlayer;

    }

    public UnionPlayer getUnionPlayer() {

        return unionPlayer;

    }

    @Override
    public boolean equals(Object o) {

        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        UnionPlayerInput that = (UnionPlayerInput) o;
        return unionPlayer.equals(that.unionPlayer);

    }

    @Override
    public int hashCode() {

        return Objects.hash(unionPlayer);

    }

}
